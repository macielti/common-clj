(ns integration.correlation-id
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.routes :as component.routes]
            [common-clj.component.service :as component.service]
            [schema.test :as s]
            [common-clj.component.helper.core :as component.helper]
            [integration.aux.http :as aux.http]
            [common-clj.traceability.core :as common-traceability]
            [mockfn.macros :as mfn]))

(def ^:private routes-example [["/correlation-id-test" :get (common-traceability/with-correlation-id (fn [_]
                                                                                                       {:status 200
                                                                                                        :body   {:correlation-id common-traceability/*correlation-id*}}))
                                :route-name :test]])

(def ^:private system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test :json)
    :routes (component/using (component.routes/new-routes routes-example)
                             [:config])
    :service (component/using (component.service/new-service)
                              [:config :routes])))

(s/deftest correlation-id-test
  (mfn/providing [(random-uuid) #uuid "1b9c8e2e-b7b8-4d25-a4fa-16bc3bb34b9a"]
                 (let [system (component/start system-test)
                       service-fn (-> (component.helper/get-component-content :service system)
                                      :io.pedestal.http/service-fn)]
                   (testing "that calling endpoint without passing X-Correlation-Id header, give us a default one"
                     (is (= {:status 200
                             :body   {:correlation-id "DEFAULT.1B9C8E2E-B7B8-4D25-A4FA-16BC3BB34B9A"}}
                            (aux.http/request-test-endpoints "/correlation-id-test" nil service-fn))))

                   (testing "that calling endpoint without passing X-Correlation-Id header, give us a default one"
                     (is (= {:status 200
                             :body   {:correlation-id "MOCK.TEST.1B9C8E2E-B7B8-4D25-A4FA-16BC3BB34B9A"}}
                            (aux.http/request-test-endpoints "/correlation-id-test" {"X-Correlation-Id" "MOCK.TEST"} service-fn))))
                   (component/stop-system system))))
