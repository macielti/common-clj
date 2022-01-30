(ns common-clj.component.kafka.consumer
  (:require [schema.core :as s]
            [cheshire.core :as json]
            [com.stuartsierra.component :as component]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.interceptor.chain :as chain]
            [plumbing.core :as plumbing])
  (:import (org.apache.kafka.clients.consumer KafkaConsumer MockConsumer OffsetResetStrategy)
           (org.apache.kafka.common TopicPartition)
           (java.time Duration)
           (org.apache.kafka.common.serialization StringDeserializer)))

(def kafka-client-starter
  (interceptor/interceptor
    {:name  ::kafka-client
     :enter (fn [{:keys [consumer-props] :as context}]
              (assoc context :kafka-client (new KafkaConsumer consumer-props)))}))

(def mock-kafka-client-starter
  (interceptor/interceptor
    {:name  ::mock-kafka-client
     :enter (fn [{:keys [_] :as context}]
              (assoc context :kafka-client (new MockConsumer OffsetResetStrategy/EARLIEST)))}))

(def mock-subscriber
  (interceptor/interceptor
    {:name  ::mock-subscriber
     :enter (fn [{:keys [topics kafka-client] :as context}]
              (doto kafka-client
                (.subscribe topics)
                (.rebalance (map #(TopicPartition. % 0) topics)))
              (doseq [topic topics]
                (.updateBeginningOffsets kafka-client {(TopicPartition. topic 0) (long 0)}))
              context)}))

(def subscriber
  (interceptor/interceptor
    {:name  ::subscriber
     :enter (fn [{:keys [topics kafka-client] :as context}]
              (.subscribe kafka-client topics)
              context)}))

(defn kafka-record->clj-message
  [record]
  {:topic (keyword (.topic record))
   :value (json/decode (.value record) true)})

(s/defn handler-by-topic
  [topic :- s/Keyword
   topic-consumers]
  (topic topic-consumers))

(def kafka-consumer!
  (interceptor/interceptor
    {:name  ::kafka-consumer
     :enter (fn [{:keys [kafka-client topic-consumers components] :as context}]
              (assoc context :loop-consumer (future
                                              (while true
                                                (let [records (seq (.poll kafka-client (Duration/ofMillis 100)))]
                                                  (doseq [record records]
                                                    (let [{:keys [topic] :as message} (kafka-record->clj-message record)
                                                          {:keys [handler]} (handler-by-topic topic topic-consumers)]
                                                      (handler message components))))))))}))

(s/defrecord Consumer [config datomic producer topic-consumers]
  component/Lifecycle

  (start [this]
    (let [current-env    (-> config :config :current-env)
          consumer-props {"value.deserializer" StringDeserializer
                          "key.deserializer"   StringDeserializer
                          "bootstrap.servers"  (get-in config [:config :bootstrap-server])
                          "group.id"           (get-in config [:config :service-name])}
          components     (plumbing/assoc-when {}
                                              :producer (:producer producer)
                                              :config (:config config)
                                              :datomic (:datomic datomic))
          topics         (get-in config [:config :topics])
          context        {:consumer-props  consumer-props
                          :topics          topics
                          :topic-consumers topic-consumers
                          :components      components}]
      (cond-> this
              (= current-env :prod) (assoc :consumer (-> (chain/execute context [kafka-client-starter subscriber kafka-consumer!])
                                                         :kafka-client))
              (= current-env :test) (assoc :consumer (-> (chain/execute context [mock-kafka-client-starter mock-subscriber kafka-consumer!])
                                                         (select-keys [:loop-consumer :kafka-client]))))))

  (stop [{{:keys [kafka-client loop-consumer]} :consumer :as this}]
    (deref loop-consumer 5000 loop-consumer)
    (.close kafka-client)
    (assoc this :consumer nil)))

(defn new-consumer [topic-consumers]
  (map->Consumer {:topic-consumers topic-consumers}))
