(ns integration.schema-in-interceptor-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.routes :as component.routes]
            [common-clj.component.service :as component.service]
            [common-clj.io.interceptors :as io.interceptors]
            [integration.aux.http :as aux.http]
            [schema.core :as s]))

(s/defschema SchemaTest
  {:username s/Str})

(def ^:private routes-example [["/schema-in-interceptor-test" :post [(io.interceptors/schema-body-in-interceptor SchemaTest)
                                                                     (fn [{{:keys [username]} :json-params}]
                                                                       {:status 200 :body {:username username}})]
                                :route-name :schema-in-interceptor-test]])

(def ^:private system-test
  (component/system-map
   :config (component.config/new-config "resources/config_test.json" :test :json)
   :routes (component/using (component.routes/new-routes routes-example) [:config])
   :service (component/using (component.service/new-service) [:config :routes])))

(deftest schema-in-interceptor-test
  (let [system     (component/start system-test)
        service-fn (-> (component.helper/get-component-content :service system)
                       :io.pedestal.http/service-fn)]
    (testing "that we can successfully make a post request respecting the expected schema for the body content"
      (is (= {:status 200
              :body   {:username "admin"}}
             (aux.http/post-request-to-test-endpoint "/schema-in-interceptor-test" {:username "admin"} service-fn))))

    (testing "that we received a nice and detailed error when the expected schema for the body content is not respected"
      (is (= {:status 422
              :body   {:detail  {:email    "disallowed-key"
                                 :username "missing-required-key"}
                       :error   "invalid-schema-in"
                       :message "The system detected that the received data is invalid"}}
             (aux.http/post-request-to-test-endpoint "/schema-in-interceptor-test" {:email "admin"} service-fn))))
    (component/stop-system system)))
