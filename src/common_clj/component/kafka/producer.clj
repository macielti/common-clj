(ns common-clj.component.kafka.producer
  (:require [cheshire.core :as json]
            [schema.core :as s]
            [com.stuartsierra.component :as component])
  (:import (org.apache.kafka.clients.producer KafkaProducer ProducerRecord MockProducer)
           (org.apache.kafka.common.serialization StringSerializer)))

(defn produce! [{:keys [topic message]} producer]
  (let [record (ProducerRecord. (name topic) (json/encode message))]
    (-> producer
        (.send record)
        .get)))

(s/defn mock-produced-messages [producer :- MockProducer]
  (->> (.history producer)
       (map (fn [record]
              {:topic (keyword (.topic record))
               :value (json/decode (.value record) true)}))))

(defrecord Producer [config]
  component/Lifecycle

  (start [this]
    (let [bootstrap-server (-> config :config :bootstrap-server)
          current-env      (-> config :config :current-env)
          producer-props   {"value.serializer"  StringSerializer
                            "key.serializer"    StringSerializer
                            "bootstrap.servers" bootstrap-server}]
      (cond-> this
              (= current-env :prod) (assoc :producer (KafkaProducer. producer-props))
              (= current-env :test) (assoc :producer (MockProducer. true (StringSerializer.) (StringSerializer.))))))

  (stop [this]
    (assoc this :producer nil)))

(defn new-producer []
  (->Producer {}))
