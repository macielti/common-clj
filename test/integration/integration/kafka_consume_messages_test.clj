(ns integration.kafka-consume-messages-test
  (:require [clojure.test :refer :all]
            [schema.test :as s-test]
            [schema.core :as s]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.kafka.consumer :as component.consumer]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.kafka.producer :as component.producer]))

(def test-state (atom nil))

(defn ^:private test-topic-consumer
  [message
   components]
  (reset! test-state message))

(s/defschema ^:private TestMessage
  {:topic (s/enum :consumer-topic-test)
   :value {:test-title s/Str}})

(def ^:private topic-consumers
  {:consumer-topic-test {:schema  TestMessage
                         :handler test-topic-consumer}})

(def ^:private system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test)
    :consumer (component/using (component.consumer/new-consumer topic-consumers) [:config])
    :producer (component/using (component.producer/new-mock-producer) [:consumer])))

(s-test/deftest kafka-consumer-component-test
  (let [system   (component/start system-test)
        producer (component.helper/get-component-content :producer system)]

    (component.producer/produce! {:topic   :consumer-topic-test
                                  :message {:test-title "just a simple test"}}
                                 producer)

    (Thread/sleep 5000)

    (testing "that we can use kafka consumer to consumer messages"
      (is (= {:topic :consumer-topic-test
              :value {:test-title "just a simple test"}}
             @test-state)))

    (reset! test-state nil)
    (component/stop system)))
