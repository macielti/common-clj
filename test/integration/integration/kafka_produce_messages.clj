(ns integration.kafka-produce-messages
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.kafka.producer :as component.producer]
            [common-clj.component.helper.core :as component.helper]
            [schema.test :as s]))

(def ^:private system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test)
    :producer (component/using (component.producer/new-producer)
                               [:config])))

(s/deftest datomic-component-test
  (let [system   (component/start system-test)
        producer (component.helper/get-component-content :producer system)]

    (component.producer/produce! {:topic   :hello
                                  :message {:test-result :ok}} producer)

    (testing "that we can use kafka producer to send messages"
      (is (= [{:topic :hello
               :value {:test-result "ok"}}]
             (component.producer/mock-produced-messages producer))))
    (component/stop system)))
