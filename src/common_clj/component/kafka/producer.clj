(ns common-clj.component.kafka.producer
  (:require [cheshire.core :as json]
            [com.stuartsierra.component :as component]
            [schema.core :as s])
  (:import (org.apache.kafka.clients.producer KafkaProducer ProducerRecord)
           (org.apache.kafka.common.serialization StringSerializer)))

(s/defschema KafkaMessageInput
  {:topic   s/Keyword
   :message {s/Keyword (s/maybe s/Any)}})

(defmulti produce!
          (fn [_ {:keys [current-env]}]
            current-env))

(s/defmethod produce! :prod
             [{:keys [topic message]} :- KafkaMessageInput
              {:keys [kafka-producer]}]
             (-> kafka-producer
                 (.send (ProducerRecord. (name topic) (json/encode message)))
                 .get))

(s/defmethod produce! :test
             [{:keys [topic message]} :- KafkaMessageInput
              {:keys [produced-messages]}]
             (swap! produced-messages conj (ProducerRecord. (name topic) (json/encode message))))

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
