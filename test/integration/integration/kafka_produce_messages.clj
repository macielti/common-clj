(ns integration.kafka-produce-messages
  (:require [clojure.test :refer :all]
            [schema.test :as s-test]
            [schema.core :as s]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.kafka.producer :as component.producer]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.kafka.consumer :as component.consumer]))

(s/defschema ^:private TestMessage
  {:topic (s/enum :consumer-topic-test)
   :value {:test-title s/Str}})

(def ^:private topic-consumers
  {:consumer-topic-test {:schema  TestMessage
                         :handler (fn [_ _] nil)}})

(def ^:private system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test :json)
    :consumer (component/using (component.consumer/new-mock-consumer topic-consumers) [:config])
    :producer (component/using (component.producer/new-mock-producer) [:consumer :config])))

(s-test/deftest kafka-producer-component-test
  (let [system   (component/start system-test)
        producer (component.helper/get-component-content :producer system)
        consumer (component.helper/get-component-content :consumer system)]

    (component.producer/produce! {:topic   :consumer-topic-test
                                  :message {:test-title "just a simple test"}}
                                 producer)

    (testing "that we can use kafka producer to send messages"
      (is (= [{:topic   :consumer-topic-test
               :message {:test-title "just a simple test"}}]
             (component.consumer/produced-messages consumer))))
    (component/stop system)))
