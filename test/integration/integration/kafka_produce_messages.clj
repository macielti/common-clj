(ns integration.kafka-produce-messages
  (:require [clojure.test :refer [is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.kafka.consumer :as component.consumer]
            [common-clj.component.kafka.producer :as component.producer]
            [schema.core :as s]
            [schema.test :as s-test]))

(s/defschema ^:private TestMessagePayload
  {:test s/Str})

(def ^:private topic-consumers
  {:consumer-topic-test {:schema  TestMessagePayload
                         :handler (fn [_ _] nil)}})

(def ^:private system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test :json)
    :producer (component/using (component.producer/new-mock-producer) [:config])
    :consumer (component/using (component.consumer/new-mock-consumer topic-consumers) [:config :producer])))

(s-test/deftest kafka-producer-component-test
  (let [system (component/start system-test)
        producer (component.helper/get-component-content :producer system)]

    (component.producer/produce! {:topic :consumer-topic-test
                                  :data  {:payload {:test "just a simple test"}}}
                                 producer)

    (testing "that we can use kafka producer to send messages"
      (is (= [{:data  {:payload {:test "just a simple test"}
                       :meta    {:correlation-id "DEFAULT"}}
               :topic :consumer-topic-test}]
             (component.producer/produced-messages producer))))
    (component/stop system)))
