(ns integration.rabbitmq-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.rabbitmq.consumer :as component.rabbitmq.consumer]
            [common-clj.component.rabbitmq.producer :as component.rabbitmq.producer]
            [schema.core :as schema]
            [schema.test :as s]))

(def test-state (atom nil))

(schema/defschema TestSchema
  {:test schema/Keyword})

(def consumers {:test.example {:schema     TestSchema
                               :handler-fn (fn [{:keys [payload]}]
                                             (reset! test-state payload))}})

(def ^:private system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.edn" :test :edn)
    :rabbitmq-producer (component/using (component.rabbitmq.producer/new-producer) [:config])
    :rabbitmq-consumer (component/using (component.rabbitmq.consumer/new-consumer consumers) [:config :rabbitmq-producer])))

(s/deftest rabbitmq-consumer-and-producer-component-test
  (let [system (component/start system-test)
        producer (component.helper/get-component-content :rabbitmq-producer system)]

    (testing "that we can use kafka consumer to consumer messages"
      (component.rabbitmq.producer/produce! {:topic   :test.example
                                             :payload {:test :ok}}
                                            producer)

      (Thread/sleep 5000)

      (is (= {:test :ok}
             @test-state))

      (reset! test-state nil))

    ;TODO: Create a helper function in order fo fetch produced messages
    (testing "that we can retrieve produced messages"
      (is (= [{:payload {:test :ok}
               :topic   :test.example}]
             @(:produced-messages producer))))

    (component/stop system)))

(s/deftest rabbitmq-consumer-and-producer-component-test-should-produce-dead-letter-on-wrong-message-schema
  (let [system (component/start system-test)
        producer (component.helper/get-component-content :rabbitmq-producer system)]

    (testing "that we can use kafka consumer to consumer messages"
      (component.rabbitmq.producer/produce! {:topic   :test.example
                                             :payload {:test 1}}
                                            producer)
      (Thread/sleep 5000))

    ;TODO: Create a helper function in order fo fetch produced messages
    (testing "that we can retrieve produced messages"
      (is (= [{:payload {:test 1}
               :topic   :test.example}
              {:payload {:exception-info "clojure.lang.ExceptionInfo: Value does not match schema: {:test (not (keyword? 1))} {:type :schema.core/error, :schema {:test Keyword}, :value {:test 1}, :error {:test (not (keyword? 1))}}"
                         :payload        {:test 1}
                         :service        "test-service-name"
                         :topic          :test.example}
               :topic   :create-dead-letter}]
             @(:produced-messages producer))))

    (component/stop system)))

(def problematic-consumers {:test.example {:schema     TestSchema
                                           :handler-fn (fn [_]
                                                         (throw (Exception. "my exception message")))}})

(def ^:private system-test-problematic-consumer-with-dead-letter-enabled
  (component/system-map
    :config (component.config/new-config "resources/config_test.edn" :test :edn)
    :rabbitmq-producer (component/using (component.rabbitmq.producer/new-producer) [:config])
    :rabbitmq-consumer (component/using (component.rabbitmq.consumer/new-consumer problematic-consumers) [:config :rabbitmq-producer])))

(s/deftest rabbitmq-consumer-and-producer-component-test-should-produce-dead-letter
  (let [system (component/start system-test-problematic-consumer-with-dead-letter-enabled)
        producer (component.helper/get-component-content :rabbitmq-producer system)]

    (testing "that we can use kafka consumer to consumer messages"
      (component.rabbitmq.producer/produce! {:topic   :test.example
                                             :payload {:test :ok}}
                                            producer)

      (Thread/sleep 5000))

    ;TODO: Create a helper function in order fo fetch produced messages
    (testing "that we can retrieve produced messages"
      (is (= [{:payload {:test :ok}
               :topic   :test.example}
              {:payload {:exception-info "java.lang.Exception: my exception message"
                         :payload        {:test :ok}
                         :service        "test-service-name"
                         :topic          :test.example}
               :topic   :create-dead-letter}]
             @(:produced-messages producer))))

    (component/stop system)))

(def ^:private system-test-problematic-consumer-with-dead-letter-disabled
  (component/system-map
    :config (component.config/new-config "resources/config_test_dead_letter_disabled.edn" :test :edn)
    :rabbitmq-producer (component/using (component.rabbitmq.producer/new-producer) [:config])
    :rabbitmq-consumer (component/using (component.rabbitmq.consumer/new-consumer problematic-consumers) [:config :rabbitmq-producer])))

(s/deftest rabbitmq-consumer-and-producer-component-test-should-not-produce-dead-letter
  (let [system (component/start system-test-problematic-consumer-with-dead-letter-disabled)
        producer (component.helper/get-component-content :rabbitmq-producer system)]

    (testing "that we can use kafka consumer to consumer messages"
      (component.rabbitmq.producer/produce! {:topic   :test.example
                                             :payload {:test :ok}}
                                            producer)

      (Thread/sleep 5000))

    ;TODO: Create a helper function in order fo fetch produced messages
    (testing "that we can retrieve produced messages"
      (is (= [{:payload {:test :ok}
               :topic   :test.example}]
             @(:produced-messages producer))))

    (component/stop system)))