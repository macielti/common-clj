(ns integration.rabbitmq-test
  (:require [clojure.test :refer [is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.rabbitmq.consumer :as component.rabbitmq.consumer]
            [common-clj.component.rabbitmq.producer :as component.rabbitmq.producer]
            [common-clj.test.helper.components.containers :as test.helper.components.containers]
            [matcher-combinators.matchers :as m]
            [matcher-combinators.test :refer [match?]]
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
   :config (component.config/new-config "test/resources/config_test.edn" :test :edn)
   :containers (test.helper.components.containers/new-containers #{:rabbitmq})
   :rabbitmq-producer (component/using (component.rabbitmq.producer/new-mock-producer) [:config :containers])
   :rabbitmq-consumer (component/using (component.rabbitmq.consumer/new-mock-rabbitmq-consumer consumers) [:config :rabbitmq-producer :containers])))

(s/deftest rabbitmq-consumer-and-producer-component-test
  (let [system (component/start system-test)
        producer (component.helper/get-component-content :rabbitmq-producer system)]

    (testing "that we can use kafka consumer to consumer messages"
      (component.rabbitmq.producer/produce! {:topic   :test.example
                                             :payload {:test :ok}}
                                            producer)

      (Thread/sleep 5000)

      (is (match? (m/equals {:meta {:correlation-id string?}
                             :test :ok})
                  @test-state))

      (reset! test-state nil))

    ;TODO: Create a helper function in order fo fetch produced messages
    (testing "that we can retrieve produced messages"
      (is (match? (m/equals [{:payload {:meta {:correlation-id string?}
                                        :test :ok}
                              :topic   :test.example}])
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
      (is (match? (m/equals [{:payload {:meta {:correlation-id string?}
                                        :test 1}
                              :topic   :test.example}
                             {:payload {:meta           {:correlation-id string?}
                                        :exception-info "clojure.lang.ExceptionInfo: Value does not match schema: {:test (not (keyword? 1))} {:type :schema.core/error, :schema {:test Keyword}, :value {:test 1}, :error {:test (not (keyword? 1))}}"
                                        :payload        {:test 1}
                                        :service        "test-service-name"
                                        :topic          :test.example}
                              :topic   :create-dead-letter}])
                  @(:produced-messages producer))))

    (component/stop system)))

(def problematic-consumers {:test.example {:schema     TestSchema
                                           :handler-fn (fn [_]
                                                         (throw (Exception. "my exception message")))}})

(def ^:private system-test-problematic-consumer-with-dead-letter-enabled
  (component/system-map
   :config (component.config/new-config "test/resources/config_test.edn" :test :edn)
   :containers (test.helper.components.containers/new-containers #{:rabbitmq})
   :rabbitmq-producer (component/using (component.rabbitmq.producer/new-mock-producer) [:config :containers])
   :rabbitmq-consumer (component/using (component.rabbitmq.consumer/new-mock-rabbitmq-consumer problematic-consumers) [:config :rabbitmq-producer :containers])))

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
      (is (match? (m/equals [{:payload {:meta {:correlation-id string?}
                                        :test :ok}
                              :topic   :test.example}
                             {:payload {:meta           {:correlation-id string?}
                                        :exception-info "java.lang.Exception: my exception message"
                                        :payload        {:test :ok}
                                        :service        "test-service-name"
                                        :topic          :test.example}
                              :topic   :create-dead-letter}])
                  @(:produced-messages producer))))

    (component/stop system)))

(def ^:private system-test-problematic-consumer-with-dead-letter-disabled
  (component/system-map
   :config (component.config/new-config "test/resources/config_test_dead_letter_disabled.edn" :test :edn)
   :containers (test.helper.components.containers/new-containers #{:rabbitmq})
   :rabbitmq-producer (component/using (component.rabbitmq.producer/new-mock-producer) [:config :containers])
   :rabbitmq-consumer (component/using (component.rabbitmq.consumer/new-mock-rabbitmq-consumer consumers) [:config :rabbitmq-producer :containers])))

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
      (is (match? (m/equals [{:payload {:meta {:correlation-id string?}
                                        :test :ok}
                              :topic   :test.example}])
                  @(:produced-messages producer))))

    (component/stop system)))
