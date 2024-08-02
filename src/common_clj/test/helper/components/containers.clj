(ns common-clj.test.helper.components.containers
  (:require [com.stuartsierra.component :as component])
  (:import (org.testcontainers.containers GenericContainer PostgreSQLContainer RabbitMQContainer)))

(defrecord Containers [containers]
  component/Lifecycle
  (start [component]
    (let [rabbitmq-container (when (:rabbitmq containers)
                               (doto (RabbitMQContainer. "rabbitmq:3-alpine")
                                 .start))

          postgresql-container (when (:postgresql containers)
                                 (doto (PostgreSQLContainer. "postgres:16-alpine")
                                   .start))]

      (merge component {:containers {:rabbitmq   rabbitmq-container
                                     :postgresql postgresql-container}})))

  (stop [component]
    (when (:rabbitmq (:containers component))
      (.stop ^GenericContainer (:rabbitmq (:containers component))))

    (when (:postgresql (:containers component))
      (.stop ^GenericContainer (:postgresql (:containers component))))

    component))

(defn new-containers [containers]
  (->Containers containers))