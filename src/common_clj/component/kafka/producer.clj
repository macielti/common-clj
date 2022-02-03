(ns common-clj.component.kafka.producer
  (:require [cheshire.core :as json]
            [schema.core :as s]
            [com.stuartsierra.component :as component])
  (:import (org.apache.kafka.clients.producer KafkaProducer ProducerRecord MockProducer)
           (org.apache.kafka.common.serialization StringSerializer)
           (org.apache.kafka.clients.consumer ConsumerRecord)))

(defn produce!
  [{:keys [topic message]}
   producer]
  (let [{:keys [kafka-producer mock-consumer]} producer]
    (-> kafka-producer
        (.send (ProducerRecord. (name topic) (json/encode message)))
        .get)
    (.addRecord mock-consumer
                (ConsumerRecord. (name topic) 0 (long 0) nil (json/encode message)))))

(s/defn mock-produced-messages [producer :- MockProducer]
  (->> (.history producer)
       (map (fn [record]
              {:topic (keyword (.topic record))
               :value (json/decode (.value record) true)}))))

(defrecord Producer [config]
  component/Lifecycle

  (start [this]
    (let [bootstrap-server (-> config :config :bootstrap-server)
          producer-props   {"value.serializer"  StringSerializer
                            "key.serializer"    StringSerializer
                            "bootstrap.servers" bootstrap-server}]

      (assoc this :producer (KafkaProducer. producer-props))))

  (stop [this]
    (assoc this :producer nil)))

(defn new-producer []
  (->Producer {}))


(defrecord MockKafkaProducer [consumer]
  component/Lifecycle

  (start [this]
    (assoc this :producer {:kafka-producer (MockProducer. true (StringSerializer.) (StringSerializer.))
                           :mock-consumer  (-> consumer :consumer :kafka-client)}))

  (stop [this]
    (assoc this :producer nil)))

(defn new-mock-producer []
  (->MockKafkaProducer {}))
