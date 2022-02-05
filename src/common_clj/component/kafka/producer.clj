(ns common-clj.component.kafka.producer
  (:require [cheshire.core :as json]
            [com.stuartsierra.component :as component])
  (:import (org.apache.kafka.clients.producer KafkaProducer ProducerRecord)
           (org.apache.kafka.common.serialization StringSerializer)))

(defn produce!
  [{:keys [topic message] :as document}
   producer]
  (let [{:keys [kafka-producer current-env produced-messages]} producer]
    (cond
      (= :prod current-env)
      (-> kafka-producer
          (.send (ProducerRecord. (name topic) (json/encode message)))
          .get)

      (= :test current-env)
      (swap! produced-messages conj document))))

(defrecord Producer [config]
  component/Lifecycle

  (start [this]
    (let [bootstrap-server (-> config :config :bootstrap-server)
          producer-props   {"value.serializer"  StringSerializer
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
