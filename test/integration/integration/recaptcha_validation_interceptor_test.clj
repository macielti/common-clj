(ns integration.recaptcha-validation-interceptor-test
  (:require [cheshire.core :as json]
            [clj-http.fake :as http.fake]
            [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.routes :as component.routes]
            [common-clj.component.service :as component.service]
            [common-clj.io.interceptors.auth :as io.interceptors.auth]
            [integration.aux.http :as aux.http]
            [schema.test :as schema-test]))

;TODO: Check if we called the right external endpoints passing the right params (need a http component that maintain a history of requests that were made during test)
;TODO: Make this integration tests not hit the actual google servers (add mock for every external http request)

(def ^:private routes-example [["/recaptcha-validation-v3-interceptor-test" :get [io.interceptors.auth/recaptcha-v3-validation-interceptor
                                                                                  (constantly {:status 200 :body nil})]
                                :route-name :recaptcha-validation-v3-interceptor-test]])

(def ^:private system-test-with-recaptcha-v3-validation-disabled
  (component/system-map
   :config (component.config/new-config "resources/config_test.json" :test :json)
   :routes (component/using (component.routes/new-routes routes-example) [:config])
   :service (component/using (component.service/new-service) [:config :routes])))

(schema-test/deftest recaptcha-validation-v3-interceptor-disabled-test
  (let [system (component/start system-test-with-recaptcha-v3-validation-disabled)
        service-fn (-> (component.helper/get-component-content :service system)
                       :io.pedestal.http/service-fn)]
    (testing "we can request the endpoint without passing the 'X-Recaptcha-Token' header if the recaptcha-validation is disabled"
      (is (= {:status 200
              :body   nil}
             (aux.http/request-test-endpoints "/recaptcha-validation-v3-interceptor-test" {} service-fn))))
    (component/stop-system system)))

(def ^:private system-test-with-recaptcha-v3-validation-enabled
  (component/system-map
   :config (component.config/new-config "resources/config.test.recaptcha_validation_enabled.edn" :test :edn)
   :routes (component/using (component.routes/new-routes routes-example) [:config])
   :service (component/using (component.service/new-service) [:config :routes])))

(def valid-recaptcha-v3-token-response "valid-recaptcha-v3-token-response")

;TODO: Add test case with a valid recaptcha response token but a too low score
(schema-test/deftest recaptcha-validation-v3-interceptor-enabled-test
  (http.fake/with-fake-routes
    {"https://www.google.com/recaptcha/api/siteverify?secret=ednaldo-pereira&response=valid-recaptcha-v3-token-response" {:post (constantly {:status 200
                                                                                                                                             :body   (json/encode {:success true
                                                                                                                                                                   :score   0.8})})}}
    (let [system (component/start system-test-with-recaptcha-v3-validation-enabled)
          service-fn (-> (component.helper/get-component-content :service system)
                         :io.pedestal.http/service-fn)]
      (testing "we can't request the endpoint without passing the 'X-Recaptcha-Token' header if the recaptcha-validation is enabled"
        (is (= {:status 400
                :body   {:error   "not-able-to-perform-recaptcha-validation"
                         :message "Not able to check the success completion of the reCAPTCHA challenge"
                         :detail  {:error "not-able-to-perform-recaptcha-validation"}}}
               (aux.http/request-test-endpoints "/recaptcha-validation-v3-interceptor-test" {} service-fn))))

      (testing "we can't request the endpoint passing the 'X-Recaptcha-Token' header with a not valid response token"
        (is (= {:status 400
                :body   {:error   "not-able-to-perform-recaptcha-validation"
                         :message "Not able to check the success completion of the reCAPTCHA challenge"
                         :detail  {:error "not-able-to-perform-recaptcha-validation"}}}
               (aux.http/request-test-endpoints "/recaptcha-validation-v3-interceptor-test" {"X-Recaptcha-Token" "random-stuff"} service-fn))))

      (testing "we can request the endpoint passing the 'X-Recaptcha-Token' header with a valid response token"
        (is (= {:status 200
                :body   nil}
               (aux.http/request-test-endpoints "/recaptcha-validation-v3-interceptor-test" {"X-Recaptcha-Token" valid-recaptcha-v3-token-response} service-fn))))
      (component/stop-system system))))
