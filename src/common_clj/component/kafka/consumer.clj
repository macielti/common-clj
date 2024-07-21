(ns common-clj.component.kafka.consumer
  (:require [camel-snake-kebab.core :as camel-snake-kebab]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [common-clj.component.kafka.adapters :as component.kafka.adapters]
            [common-clj.component.kafka.models :as component.kafka.models]
            [common-clj.component.kafka.producer :as component.kafka.producer]
            [common-clj.traceability.core :as common-traceability]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.interceptor.chain :as chain]
            [medley.core :as medley]
            [overtone.at-at :as at-at]
            [schema.core :as s]
            [taoensso.timbre :as timbre])
  (:import (java.time Duration)
           (org.apache.kafka.clients.consumer KafkaConsumer)
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

(s/defn handler-by-topic
  [topic :- s/Keyword
   topic-consumers]
  (topic topic-consumers))

(s/defn replay-dead-letter!
  [{:keys [topic data]} :- component.kafka.models/KafkaMessage
   service-name
   exception-info :- s/Str
   producer]
  (component.kafka.producer/produce! {:topic :create-dead-letter
                                      :data  {:payload {:service       (camel-snake-kebab/->SCREAMING_SNAKE_CASE_STRING service-name)
                                                        :topic         (camel-snake-kebab/->SCREAMING_SNAKE_CASE_STRING topic)
                                                        :exceptionInfo exception-info
                                                        :payload       (json/encode (:payload data))}
                                              :meta    {:correlation-id (common-traceability/current-correlation-id)}}}
                                     producer))

(def kafka-consumer!
  (interceptor/interceptor
    {:name  ::kafka-consumer
     :enter (fn [{:keys [kafka-client topic-consumers components service-name] :as context}]
              (assoc context :loop-consumer (future
                                              (while true
                                                (let [records (seq (.poll kafka-client (Duration/ofMillis 100)))]
                                                  (doseq [record records]
                                                    (let [{:keys [topic data] :as clj-message} (component.kafka.adapters/kafka-record->clj-message record)
                                                          {:keys [handler schema]} (handler-by-topic topic topic-consumers)]
                                                      (try
                                                        (s/validate schema (:payload data))
                                                        (binding [common-traceability/*correlation-id* (-> data :meta :correlation-id
                                                                                                           (or (common-traceability/current-correlation-id))
                                                                                                           common-traceability/correlation-id-appended)]
                                                          (handler (:payload data) components))
                                                        (catch Exception e
                                                          (do (log/error e)
                                                              (when (-> components :config :dead-letter-queue-service-integration-enabled)
                                                                (replay-dead-letter! clj-message service-name (str e) (:producer components)))))))))))))}))

(s/defrecord Consumer [config datomic producer topic-consumers]
  component/Lifecycle

  (start [this]
    (let [consumer-props {"value.deserializer" StringDeserializer
                          "key.deserializer"   StringDeserializer
                          "bootstrap.servers"  (get-in config [:config :bootstrap-server])
                          "group.id"           (get-in config [:config :service-name])}
          components (medley/assoc-some {}
                                        :producer (:producer producer)
                                        :config (:config config)
                                        :datomic (:datomic datomic))
          topics (get-in config [:config :topics])
          context {:consumer-props  consumer-props
                   :topics          topics
                   :topic-consumers topic-consumers
                   :components      components
                   :service-name    (get-in config [:config :service-name])}]

      (when-not topics
        (timbre/error :kafka-topics-not-configured))

      (assoc this :consumer (-> (chain/execute context [kafka-client-starter subscriber kafka-consumer!])
                                :kafka-client))))

  (stop [{{:keys [kafka-client]} :consumer :as this}]
    (.close kafka-client)
    (assoc this :consumer nil)))

(defn new-consumer [topic-consumers]
  (map->Consumer {:topic-consumers topic-consumers}))

(defn consumed-messages
  [{:keys [consumed-messages]}]
  (mapv component.kafka.adapters/kafka-record->clj-message @consumed-messages))

(defn ^:private commit-message-as-consumed
  [message
   consumed-messages]
  (swap! consumed-messages conj message))

(s/defn ^:private messages-that-were-produced-but-not-consumed-yet
  [topics :- #{s/Keyword}
   produced-messages
   consumed-messages]
  (-> (filterv #(topics (keyword (.topic %))) produced-messages)
      (clojure.set/difference consumed-messages)))

(defrecord MockKafkaConsumer [config datomic producer topic-consumers]
  component/Lifecycle

  (start [this]
    (when-not (:producer producer)
      (throw (ex-info "MockKafkaConsumer depends on MockKafkaProducer"
                      {:error :mock-kafka-producer-component-not-provided})))
    (let [components (medley/assoc-some {}
                                          :producer (:producer producer)
                                          :config (:config config)
                                          :datomic (:datomic datomic))
          consumed-messages (atom [])
          produced-messages (-> components :producer :produced-messages)
          topics (->> components :config :topics (map keyword) set)
          service-name (-> components :config :service-name)
          consumer-pool (at-at/mk-pool)]

      (at-at/interspaced 100 (fn []
                               (doseq [message-record (messages-that-were-produced-but-not-consumed-yet topics @produced-messages @consumed-messages)]
                                 (let [{:keys [topic data] :as clj-message} (component.kafka.adapters/kafka-record->clj-message message-record)
                                       {:keys [handler schema]} (handler-by-topic topic topic-consumers)]
                                   (try
                                     (s/validate schema (:payload data))
                                     (binding [common-traceability/*correlation-id* (-> data :meta :correlation-id
                                                                                        (or (common-traceability/current-correlation-id))
                                                                                        common-traceability/correlation-id-appended)]
                                       (handler (:payload data) components))
                                     (catch Exception e
                                       (do (log/error e)
                                           (when (-> components :config :dead-letter-queue-service-integration-enabled)
                                             (replay-dead-letter! clj-message service-name (str e) (:producer components)))))
                                     (finally (commit-message-as-consumed message-record consumed-messages)))))) consumer-pool)

      (assoc this :consumer {:consumed-messages consumed-messages
                             :consumer-pool     consumer-pool})))

  (stop [{{:keys [consumer-pool]} :consumer :as this}]
    (at-at/stop-and-reset-pool! consumer-pool)
    (assoc this :consumer nil)))

(defn new-mock-consumer [topic-consumers]
  (->MockKafkaConsumer {} {} {} topic-consumers))
