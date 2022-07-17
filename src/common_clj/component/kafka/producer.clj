(ns common-clj.component.kafka.producer
  (:require [cheshire.core :as json]
            [com.stuartsierra.component :as component]
            [common-clj.component.kafka.adapters :as component.kafka.adapters]
            [common-clj.component.kafka.models :as component.kafka.models]
            [schema.core :as s])
  (:import (org.apache.kafka.clients.producer KafkaProducer ProducerRecord)
           (org.apache.kafka.common.serialization StringSerializer)))

(defmulti produce!
  (fn [_ {:keys [current-env]}]
    current-env))

(s/defmethod produce! :prod
  [{:keys [topic data]} :- component.kafka.models/KafkaMessage
   {:keys [kafka-producer]}]
  (-> kafka-producer
      (.send (ProducerRecord. (name topic) (json/encode data)))
      .get))

(s/defmethod produce! :test
  [{:keys [topic data]} :- component.kafka.models/KafkaMessage
   {:keys [produced-messages]}]
  (swap! produced-messages conj (ProducerRecord. (name topic) (json/encode data))))

(defn produced-messages
  [{:keys [produced-messages]}]
  (mapv component.kafka.adapters/kafka-record->clj-message @produced-messages))

(defrecord Producer [config]
  component/Lifecycle

  (start [this]
    (let [bootstrap-server (-> config :config :bootstrap-server)
          producer-props {"value.serializer"  StringSerializer
                          "key.serializer"    StringSerializer
                          "bootstrap.servers" bootstrap-server}]

      (assoc this :producer {:kafka-producer (KafkaProducer. producer-props)
                             :current-env    (-> config :config :current-env)})))

  (stop [this]
    (assoc this :producer nil)))

(defn new-producer []
  (->Producer {}))

(defrecord MockKafkaProducer [config]
  component/Lifecycle

  (start [this]
    (let [produced-messages (atom [])]
      (assoc this :producer {:produced-messages produced-messages
                             :current-env       (-> config :config :current-env)})))

  (stop [this]
    (assoc this :producer nil)))

(defn new-mock-producer []
  (->MockKafkaProducer {}))
