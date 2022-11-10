(ns integration.rabbitmq-component-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.rabbitmq.core :as component.rabbitmq]
            [schema.test :as s]
            [common-clj.component.helper.core :as component.helper]))

(defn message-handler
  [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
  (println (format "[consumer] Received a message: %s, delivery tag: %d, content type: %s, type: %s"
                   (String. payload "UTF-8") delivery-tag content-type type)))

(def consumers {:common-clj.test-queue {:schema nil
                                        :handler message-handler}})

(def system-components
  (component/system-map
    :config (component.config/new-config "resources/config_test.edn" :test :edn)
    :rabbitmq (component/using (component.rabbitmq/new-rabbitmq consumers) [:config])))

(s/deftest rabbitmq-component-test
  (let [system (component/start system-components)
        rabbitmq (component.helper/get-component-content :rabbitmq system)]

    (component.rabbitmq/produce! {:topic :common-clj.test-queue
                                  :data  {:payload {:test "just a simple test"}}} rabbitmq)

    (Thread/sleep 1000)

    (component/stop-system system)))