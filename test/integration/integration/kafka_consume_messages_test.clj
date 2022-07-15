(ns integration.kafka-consume-messages-test
  (:require [clojure.test :refer :all]
            [schema.test :as s-test]
            [schema.core :as s]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.kafka.consumer :as component.consumer]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.kafka.producer :as component.producer]
            [clojure.tools.logging :as log]))

(def test-state (atom nil))

(defn ^:private test-topic-consumer
  [message
   _components]
  (reset! test-state message))

(s/defschema ^:private TestMessagePayload
  {:test s/Str})

(def ^:private topic-consumers
  {:consumer-topic-test {:schema  TestMessagePayload
                         :handler test-topic-consumer}})

(def ^:private system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test :json)
    :consumer (component/using (component.consumer/new-mock-consumer topic-consumers) [:config])
    :producer (component/using (component.producer/new-mock-producer) [:consumer :config])))

(s-test/deftest kafka-consumer-component-test
  (let [system (component/start system-test)
        producer (component.helper/get-component-content :producer system)]

    (testing "that we can use kafka consumer to consumer messages"
      (component.producer/produce! {:topic :consumer-topic-test
                                    :data  {:payload {:test "just a simple test"}}}
                                   producer)
      (Thread/sleep 5000)
      (is (= {:test "just a simple test"}
             @test-state))
      (reset! test-state nil))

    (component/stop system)))

(s-test/deftest kafka-consumer-component-only-consumes-interested-topics
  (let [system (component/start system-test)
        producer (component.helper/get-component-content :producer system)]

    (testing "that mock consumer only consumes messages from topics defined in config file"
      (component.producer/produce! {:topic :not-interesting-topic-test
                                    :data  {:payload {:test "just a simple test"}}}
                                   producer)
      (Thread/sleep 5000)
      (is (= nil
             @test-state))
      (reset! test-state nil))

    (component/stop system)))

(s-test/deftest kafka-consumer-component-test-wrong-schema
  (let [system (component/start system-test)
        producer (component.helper/get-component-content :producer system)]

    (component.producer/produce! {:topic :consumer-topic-test
                                  :data  {:payload {:wrong-keyword "just a simple test"}}}
                                 producer)
    (Thread/sleep 5000)

    (testing "that we can consume a message with wrong payload for consumer schema"

      (is (= nil
             @test-state)))

    (reset! test-state nil)
    (component/stop system)))
