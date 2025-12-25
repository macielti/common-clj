(ns traceability-test
  (:require [clojure.string :as str]
            [clojure.test :refer [is testing]]
            [common-clj.integrant-components.config :as component.config]
            [common-clj.integrant-components.routes :as component.routes]
            [common-clj.traceability.core :as traceability]
            [integrant.core :as ig]
            [io.pedestal.connector.test :as test]
            [io.pedestal.service.interceptors :as pedestal.service.interceptors]
            [schema.test :as s]
            [service-component.core :as component.service]))

(def routes [["/test" :get [traceability/with-correlation-id-http-interceptor
                            pedestal.service.interceptors/json-body
                            (fn [_context]
                              {:status 200
                               :body   {:cid (traceability/current-correlation-id!)}})]
              :route-name :test]])

(def system-components
  {::component.config/config   {:path "test/resources/config.example.edn"
                                :env  :test}
   ::component.routes/routes   {:routes routes}
   ::component.service/service {:components {:config (ig/ref ::component.config/config)
                                             :routes (ig/ref ::component.routes/routes)}}})

(s/deftest traceability-test
  (let [system (ig/init system-components)
        conector (-> system ::component.service/service)]

    (testing "That we can fetch the test endpoint and access correlation-id"
      (is (str/includes? (-> (test/response-for conector :get "/test")
                             :body)
                         "{\"cid\":\"DEFAULT")))

    (testing "That we can fetch the test endpoint and compose the current correlation-id based on the header"
      (is (str/includes? (-> (test/response-for conector :get "/test" :headers {"x-correlation-id" "DEFAULT.29A296ED8418.CB66A52273E6"})
                             :body)
                         "DEFAULT.29A296ED8418.CB66A52273E6")))

    (ig/halt! system)))
