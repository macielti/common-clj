(ns integration.rabbitmq-component-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.rabbitmq.core :as component.rabbitmq]
            [schema.test :as s]
            [common-clj.component.helper.core :as component.helper]
            [langohr.queue :as lq]
            [langohr.basic :as lb]
            [langohr.consumers :as lc]))

(def system-components
  (component/system-map
    :config (component.config/new-config "resources/config_test.edn" :test :edn)
    :rabbitmq (component/using (component.rabbitmq/new-rabbitmq) [:config])))

(defn message-handler
  [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
  (println (format "[consumer] Received a message: %s, delivery tag: %d, content type: %s, type: %s"
                   (String. payload "UTF-8") delivery-tag content-type type)))

(s/deftest rabbitmq-component-test
  (let [system (component/start system-components)
        rabbitmq (component.helper/get-component-content :rabbitmq system)]

    (lq/declare (:channel rabbitmq) "test.nothing" {:exclusive false :auto-delete false})

    (lc/subscribe (:channel rabbitmq) "test.nothing" message-handler {:auto-ack true})

    (lb/publish (:channel rabbitmq) "" "test.nothing" "Hello!")

    (component/stop-system system)))