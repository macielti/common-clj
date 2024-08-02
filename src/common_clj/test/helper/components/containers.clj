(ns common-clj.test.helper.components.containers
  (:require [com.stuartsierra.component :as component])
  (:import (org.testcontainers.containers GenericContainer RabbitMQContainer)))

(defrecord Containers []
  component/Lifecycle
  (start [component]
    (let [rabbit-mq-container (doto (RabbitMQContainer. "rabbitmq:3-alpine")
                                .start)]

      (merge component {:containers {:rabbitmq rabbit-mq-container}})))

  (stop [component]
    (when (:rabbitmq (:containers component))
      (.stop ^GenericContainer (:rabbitmq (:containers component))))
    component))

(defn new-containers []
  (->Containers))