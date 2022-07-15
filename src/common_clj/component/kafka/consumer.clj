(ns common-clj.component.kafka.consumer
  (:require [schema.core :as s]
            [cheshire.core :as json]
            [overtone.at-at :as at-at]
            [plumbing.core :as plumbing]
            [com.stuartsierra.component :as component]
            [common-clj.component.kafka.models :as component.kafka.models]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.interceptor.chain :as chain]
            [taoensso.timbre :as timbre]
            [clojure.tools.logging :as log])
  (:import (org.apache.kafka.clients.consumer KafkaConsumer)
           (java.time Duration)
           (org.apache.kafka.common.serialization StringDeserializer)))

(def kafka-client-starter
  (interceptor/interceptor
    {:name  ::kafka-client
     :enter (fn [{:keys [consumer-props] :as context}]
              (assoc context :kafka-client (new KafkaConsumer consumer-props)))}))

(def subscriber
  (interceptor/interceptor
    {:name  ::subscriber
     :enter (fn [{:keys [topics kafka-client] :as context}]
              (.subscribe kafka-client topics)
              context)}))

(s/defn kafka-record->clj-message :- component.kafka.models/KafkaMessage
  [record]
  (let [message (json/decode (.value record) true)]
    {:topic (keyword (.topic record))
     :data  {:payload (:payload message)}}))

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
                                                    (let [{:keys [topic data]} (kafka-record->clj-message record)
                                                          {:keys [handler schema]} (handler-by-topic topic topic-consumers)]
                                                      (try
                                                        (do (s/validate schema (:payload data))
                                                            (handler (:payload data) components))
                                                        (catch Exception e
                                                          (log/error e))))))))))}))

(s/defrecord Consumer [config datomic producer topic-consumers]
  component/Lifecycle

  (start [this]
    (let [consumer-props {"value.deserializer" StringDeserializer
                          "key.deserializer"   StringDeserializer
                          "bootstrap.servers"  (get-in config [:config :bootstrap-server])
                          "group.id"           (get-in config [:config :service-name])}
          components (plumbing/assoc-when {}
                                          :producer (:producer producer)
                                          :config (:config config)
                                          :datomic (:datomic datomic))
          topics (get-in config [:config :topics])
          context {:consumer-props  consumer-props
                   :topics          topics
                   :topic-consumers topic-consumers
                   :components      components}]

      (when-not topics
        (timbre/error :kafka-topics-not-configured))

      (assoc this :consumer (-> (chain/execute context [kafka-client-starter subscriber kafka-consumer!])
                                :kafka-client))))

  (stop [{{:keys [kafka-client]} :consumer :as this}]
    (.close kafka-client)
    (assoc this :consumer nil)))

(defn new-consumer [topic-consumers]
  (map->Consumer {:topic-consumers topic-consumers}))

(defn produced-messages
  [{:keys [produced-messages]}]
  (map kafka-record->clj-message @produced-messages))

(defn consumed-messages
  [{:keys [consumed-messages]}]
  @consumed-messages)

(defn ^:private commit-message-as-consumed
  [message
   consumed-messages]
  (swap! consumed-messages conj message))

(defn ^:private messages-that-were-produced-but-not-consumed-yet
  [produced-messages
   consumed-messages]
  (clojure.set/difference produced-messages consumed-messages))

(defrecord MockKafkaConsumer [config datomic producer topic-consumers]
  component/Lifecycle

  (start [this]
    (let [produced-messages (atom [])
          consumed-messages (atom [])
          components (plumbing/assoc-when {}
                                          :producer (:producer producer)
                                          :config (:config config)
                                          :datomic (:datomic datomic))
          consumer-pool (at-at/mk-pool)]

      (at-at/interspaced 100 (fn []
                               (doseq [message-record (messages-that-were-produced-but-not-consumed-yet @produced-messages @consumed-messages)]
                                 (let [{:keys [topic data]} (kafka-record->clj-message message-record)
                                       {:keys [handler schema]} (handler-by-topic topic topic-consumers)]
                                   (try
                                     (do (s/validate schema (:payload data))
                                         (handler (:payload data) components)
                                         (commit-message-as-consumed message-record consumed-messages))
                                     (catch Exception e
                                       (log/error e)))))) consumer-pool)

      (assoc this :consumer {:produced-messages produced-messages
                             :consumed-messages consumed-messages
                             :consumer-pool     consumer-pool})))

  (stop [{{:keys [consumer-pool]} :consumer :as this}]
    (at-at/stop-and-reset-pool! consumer-pool)
    (assoc this :consumer nil)))

(defn new-mock-consumer [topic-consumers]
  (->MockKafkaConsumer {} {} {} topic-consumers))
