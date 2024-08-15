(ns integration.kafka-consume-messages-test
  (:require [clojure.test :refer [is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.kafka.consumer :as component.consumer]
            [common-clj.component.kafka.producer :as component.producer]
            [common-clj.traceability.core :as common-traceability]
            [matcher-combinators.matchers :as m]
            [matcher-combinators.test :refer [match?]]
            [mockfn.macros :as mfn]
            [schema.core :as s]
            [schema.test :as s-test])
  (:import (clojure.lang ExceptionInfo)))

(def test-state (atom nil))

(defn ^:private test-topic-consumer
  [message
   _components]
  (reset! test-state (assoc message :cid (common-traceability/current-correlation-id))))

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
  (mfn/providing [(random-uuid) #uuid "1b9c8e2e-b7b8-4d25-a4fa-16bc3bb34b9a"]
                 (let [system (component/start system-test)
                       producer (component.helper/get-component-content :producer system)
                       consumer (component.helper/get-component-content :consumer system)]

                   (testing "that we can use kafka consumer to consumer messages"
                     (component.producer/produce! {:topic :consumer-topic-test
                                                   :data  {:payload {:test "just a simple test"}}}
                                                  producer)
                     (Thread/sleep 5000)
                     (is (= {:test "just a simple test"
                             :cid  "DEFAULT.16BC3BB34B9A.16BC3BB34B9A"}
                            @test-state))
                     (reset! test-state nil))

                   (testing "that we can retrieve consumed messages using the helper function"
                     (is (= [{:data  {:meta    {:correlation-id "DEFAULT.16BC3BB34B9A"}
                                      :payload {:test "just a simple test"}}
                              :topic :consumer-topic-test}]
                            (component.consumer/consumed-messages consumer))))

                   (component/stop system))))

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

;TODO: The dead-letter should carry on the previous correlation-id
(s-test/deftest kafka-consumer-component-test-wrong-schema
  (let [system (component/start system-test)
        producer (component.helper/get-component-content :producer system)]

    (binding [common-traceability/*correlation-id* "DEFAULT.TEST"]
      (component.producer/produce! {:topic :consumer-topic-test
                                    :data  {:payload {:wrong-keyword "just a simple test"}}}
                                   producer))
    (Thread/sleep 5000)

    (testing "that we can't consume a message with wrong payload for consumer schema"
      (is (= nil
             @test-state))
      (reset! test-state nil)

      (testing "that we produced a message to the DLQ service"
        (is (match? (m/equals [{:topic :consumer-topic-test
                                :data  {:payload {:wrong-keyword "just a simple test"}
                                        :meta    {:correlation-id "DEFAULT.TEST"}}}
                               {:topic :create-dead-letter
                                :data  {:payload {:service "TEST_SERVICE"
                                                  :topic   "CONSUMER_TOPIC_TEST"
                                                  :exceptionInfo
                                                  "clojure.lang.ExceptionInfo: Value does not match schema: {:test missing-required-key, :wrong-keyword disallowed-key} {:type :schema.core/error, :schema {:test java.lang.String}, :value {:wrong-keyword \"just a simple test\"}, :error {:test missing-required-key, :wrong-keyword disallowed-key}}"
                                                  :payload "{\"wrong-keyword\":\"just a simple test\"}"}
                                        :meta    {:correlation-id string?}}}])
                    (component.producer/produced-messages producer)))))
    (component/stop system)))

(def ^:private system-test-invalid-consumer
  (component/system-map
   :config (component.config/new-config "resources/config_test.json" :test :json)
   :consumer (component/using (component.consumer/new-mock-consumer topic-consumers) [:config])
   :producer (component/using (component.producer/new-mock-producer) [:config :consumer])))

(s-test/deftest throw-exception-when-producer-component-is-not-provided
  (testing "that we throw an exception if the producer component is not provided for consumer"
    (is (match? {:system-key :consumer}
                (try (component/start system-test-invalid-consumer)
                     (catch ExceptionInfo ex
                       (ex-data ex)))))))

(def ^:private system-test-disabled-dlq-service-integration
  (component/system-map
   :config (component.config/new-config "resources/config_test_dead_letter_disabled.json" :test :json)
   :producer (component/using (component.producer/new-mock-producer) [:config])
   :consumer (component/using (component.consumer/new-mock-consumer topic-consumers) [:config :producer])))

(s-test/deftest kafka-consumer-component-test-wrong-schema-dlq-disabled
  (let [system (component/start system-test-disabled-dlq-service-integration)
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
        (is (match? (m/equals [{:topic :consumer-topic-test
                                :data  {:payload {:wrong-keyword "just a simple test"}
                                        :meta    {:correlation-id string?}}}])
                    (component.producer/produced-messages producer)))))
    (component/stop system)))
