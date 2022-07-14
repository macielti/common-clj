(ns common-clj.component.kafka.producer
  (:require [cheshire.core :as json]
            [com.stuartsierra.component :as component]
            [schema.core :as s]
            [common-clj.component.kafka.models :as component.kafka.models])
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


(defrecord MockKafkaProducer [consumer config]
  component/Lifecycle

  (start [this]
    (assoc this :producer {:produced-messages (-> consumer :consumer :produced-messages)
                           :current-env       (-> config :config :current-env)}))

  (stop [this]
    (assoc this :producer nil)))

(defn new-mock-producer []
  (->MockKafkaProducer {} {}))
