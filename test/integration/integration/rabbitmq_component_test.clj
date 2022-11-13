(ns integration.rabbitmq-component-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.rabbitmq.core :as component.rabbitmq]
            [schema.core :as s]
            [schema.test]
            [common-clj.component.helper.core :as component.helper]))

(defn message-handler
  [payload
   components]
  (println (format "[consumer] Received a message: %s" payload)))

(def consumers {:common-clj.test-queue {:schema {:test s/Str}
                                        :handler message-handler}})

(def system-components
  (component/system-map
    :config (component.config/new-config "resources/config_test.edn" :test :edn)
    :rabbitmq (component/using (component.rabbitmq/new-rabbitmq consumers) [:config])))

(schema.test/deftest rabbitmq-component-test
  (let [system (component/start system-components)
        rabbitmq (component.helper/get-component-content :rabbitmq system)]

    (component.rabbitmq/produce! {:topic :common-clj.test-queue
                                  :data  {:payload {:test "just a simple test"}}} rabbitmq)

    (Thread/sleep 1000)

    (component/stop-system system)))