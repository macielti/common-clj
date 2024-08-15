(ns integration.correlation-id
  (:require [clojure.test :refer [is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.kafka.consumer :as component.consumer]
            [common-clj.component.kafka.producer :as component.producer]
            [common-clj.component.routes :as component.routes]
            [common-clj.component.service :as component.service]
            [common-clj.traceability.core :as common-traceability]
            [integration.aux.http :as aux.http]
            [mockfn.macros :as mfn]
            [schema.core :as s-schema]
            [schema.test :as s]))

(def test-state (atom nil))

(defn test-topic-consumer
  [_message
   _components]
  (reset! test-state (common-traceability/current-correlation-id)))

(s-schema/defschema TestMessagePayload s-schema/Any)

(def topic-consumers
  {:consumer-topic-test {:schema  TestMessagePayload
                         :handler test-topic-consumer}})

(def ^:private routes-example [["/correlation-id-test" :get (common-traceability/http-with-correlation-id (fn [{{:keys [producer]} :components}]
                                                                                                            (component.producer/produce! {:topic :consumer-topic-test
                                                                                                                                          :data  {:payload {:test (common-traceability/current-correlation-id)}}}
                                                                                                                                         producer)
                                                                                                            {:status 200
                                                                                                             :body   {:correlation-id (common-traceability/current-correlation-id)}}))
                                :route-name :test]])

(def ^:private system-test
  (component/system-map
   :config (component.config/new-config "resources/config_test.json" :test :json)
   :routes (component/using (component.routes/new-routes routes-example) [:config])
   :producer (component/using (component.producer/new-mock-producer) [:config])
   :consumer (component/using (component.consumer/new-mock-consumer topic-consumers) [:config :producer])
   :service (component/using (component.service/new-service) [:config :routes :producer])))

(s/deftest correlation-id-default-test
  (mfn/providing [(random-uuid) #uuid "1b9c8e2e-b7b8-4d25-a4fa-16bc3bb34b9a"]
                 (let [system (component/start system-test)
                       service-fn (-> (component.helper/get-component-content :service system)
                                      :io.pedestal.http/service-fn)]
                   (testing "that calling endpoint without passing X-Correlation-Id header, give us a default one"
                     (reset! test-state nil)
                     (is (= {:status 200
                             :body   {:correlation-id "DEFAULT.16BC3BB34B9A.16BC3BB34B9A"}}
                            (aux.http/request-test-endpoints "/correlation-id-test" nil service-fn)))

                     (is (= "DEFAULT.16BC3BB34B9A.16BC3BB34B9A.16BC3BB34B9A"
                            (do (Thread/sleep 5000)
                                @test-state))))
                   (component/stop-system system))))

(s/deftest correlation-id-provided-test
  (mfn/providing [(random-uuid) #uuid "1b9c8e2e-b7b8-4d25-a4fa-16bc3bb34b9a"]
                 (let [system (component/start system-test)
                       service-fn (-> (component.helper/get-component-content :service system)
                                      :io.pedestal.http/service-fn)]

                   (testing "that calling endpoint without passing X-Correlation-Id header, give us a default one"
                     (reset! test-state nil)
                     (is (= {:status 200
                             :body   {:correlation-id "MOCK.TEST.16BC3BB34B9A"}}
                            (aux.http/request-test-endpoints "/correlation-id-test" {"X-Correlation-Id" "MOCK.TEST"} service-fn)))

                     (is (= "MOCK.TEST.16BC3BB34B9A.16BC3BB34B9A"
                            (do (Thread/sleep 5000)
                                @test-state))))
                   (component/stop-system system))))
