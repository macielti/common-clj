(ns integration.recaptcha-validation-interceptor-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.routes :as component.routes]
            [common-clj.component.service :as component.service]
            [schema.test :as schema-test]
            [common-clj.component.helper.core :as component.helper]
            [integration.aux.http :as aux.http]
            [common-clj.io.interceptors.auth :as io.interceptors.auth]
            [clj-http.fake :as http.fake]
            [cheshire.core :as json]))

;TODO: Check if we called the right external endpoints passing the right params (need a http component that maintain a history of requests that were made during test)

(def ^:private routes-example [["/recaptcha-validation-interceptor-test" :get [io.interceptors.auth/recaptcha-validation-interceptor
                                                                               (constantly {:status 200 :body nil})]
                                :route-name :recaptcha-validation-interceptor-test]])

(def ^:private system-test-with-recaptcha-validation-disabled
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test :json)
    :routes (component/using (component.routes/new-routes routes-example) [:config])
    :service (component/using (component.service/new-service) [:config :routes])))

(schema-test/deftest recaptcha-validation-interceptor-disabled-test
  (let [system (component/start system-test-with-recaptcha-validation-disabled)
        service-fn (-> (component.helper/get-component-content :service system)
                       :io.pedestal.http/service-fn)]
    (testing "we can request the endpoint without passing the 'X-Recaptcha-Token' header if the recaptcha-validation is disabled"
      (is (= {:status 200
              :body   nil}
             (aux.http/request-test-endpoints "/recaptcha-validation-interceptor-test" {} service-fn))))
    (component/stop-system system)))

(def ^:private system-test-with-recaptcha-validation-enabled
  (component/system-map
    :config (component.config/new-config "resources/config.test.recaptcha_validation_enabled.edn" :test :edn)
    :routes (component/using (component.routes/new-routes routes-example) [:config])
    :service (component/using (component.service/new-service) [:config :routes])))

(def valid-recaptcha-token-response "valid-recaptcha-token-response")

(schema-test/deftest recaptcha-validation-interceptor-enabled-test
  (http.fake/with-fake-routes
    {"https://www.google.com/recaptcha/api/siteverify?secret=ednaldo-pereira&response=valid-recaptcha-token-response" {:post (constantly {:status 200
                                                                                                                                          :body   (json/encode {:success true
                                                                                                                                                                :score   0.8})})}}
    (let [system (component/start system-test-with-recaptcha-validation-enabled)
          service-fn (-> (component.helper/get-component-content :service system)
                         :io.pedestal.http/service-fn)]
      (testing "we can't request the endpoint without passing the 'X-Recaptcha-Token' header if the recaptcha-validation is enabled"
        (is (= {:status 400
                :body   {:error   "not-able-to-perform-recaptcha-validation"
                         :message "Not able to check the success completion of the reCAPTCHA challenge"
                         :detail  {:error "not-able-to-perform-recaptcha-validation"}}}
               (aux.http/request-test-endpoints "/recaptcha-validation-interceptor-test" {} service-fn))))

      (testing "we can't request the endpoint passing the 'X-Recaptcha-Token' header with a not valid response token"
        (is (= {:status 400
                :body   {:error   "not-able-to-perform-recaptcha-validation"
                         :message "Not able to check the success completion of the reCAPTCHA challenge"
                         :detail  {:error "not-able-to-perform-recaptcha-validation"}}}
               (aux.http/request-test-endpoints "/recaptcha-validation-interceptor-test" {"X-Recaptcha-Token" "random-stuff"} service-fn))))

      (testing "we can request the endpoint passing the 'X-Recaptcha-Token' header with a valid response token"
        (is (= {:status 200
                :body   nil}
               (aux.http/request-test-endpoints "/recaptcha-validation-interceptor-test" {"X-Recaptcha-Token" valid-recaptcha-token-response} service-fn))))
      (component/stop-system system))))