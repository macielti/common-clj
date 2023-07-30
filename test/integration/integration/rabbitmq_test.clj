(ns integration.rabbitmq-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.rabbitmq.consumer :as component.rabbitmq.consumer]
            [common-clj.component.rabbitmq.producer :as component.rabbitmq.producer]
            [schema.test :as s]))

(def test-state (atom nil))

(def consumers {:test.example {:handler-fn (fn [{:keys [payload]}]
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