(ns integration.kafka-consume-messages-test
  (:require [clojure.test :refer :all]
            [schema.test :as s-test]
            [schema.core :as s]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.kafka.consumer :as component.consumer]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.kafka.producer :as component.producer]
            [matcher-combinators.test :refer [match?]]))

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
    :producer (component/using (component.producer/new-mock-producer) [:config])
    :consumer (component/using (component.consumer/new-mock-consumer topic-consumers) [:config :producer])))

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

    (testing "that we can't consume a message with wrong payload for consumer schema"
      (is (= nil
             @test-state))
      (reset! test-state nil)

      (testing "that we produced a message to the DLQ service"
        (is (= [{:topic :consumer-topic-test
                 :data  {:payload {:wrong-keyword "just a simple test"}}}
                {:topic :create-dead-letter
                 :data  {:payload {:service "TEST_SERVICE"
                                   :topic   "CONSUMER_TOPIC_TEST"
                                   :exceptionInfo
                                   "clojure.lang.ExceptionInfo: Value does not match schema: {:test missing-required-key, :wrong-keyword disallowed-key} {:type :schema.core/error, :schema {:test java.lang.String}, :value {:wrong-keyword \"just a simple test\"}, :error {:test missing-required-key, :wrong-keyword disallowed-key}}"
                                   :payload "{\"wrong-keyword\":\"just a simple test\"}"}}}]
               (component.producer/produced-messages producer)))))
    (component/stop system)))
