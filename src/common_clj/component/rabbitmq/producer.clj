(ns common-clj.component.rabbitmq.producer
  (:require [com.stuartsierra.component :as component]
            [common-clj.traceability.core :as common-traceability]
            [langohr.basic :as lb]
            [langohr.channel :as lch]
            [langohr.core :as rmq]
            [schema.core :as s]))

(s/defn ^:deprecated topic->raw-topic :- s/Str
  [topic :- s/Keyword]
  (let [raw-topic-namespace (namespace topic)
        raw-topic-keyword (name topic)]
    (if raw-topic-namespace
      (str raw-topic-namespace "/" raw-topic-keyword)
      raw-topic-keyword)))

(defmulti ^:deprecated produce!
  (fn [_ {:keys [current-env]}]
    current-env))

(s/defmethod produce! :prod
  [{:keys [topic payload]}
   {:keys [channel]}]
  (let [payload' (assoc payload :meta {:correlation-id (-> (common-traceability/current-correlation-id)
                                                           common-traceability/correlation-id-appended)})]
    (lb/publish channel "" (topic->raw-topic topic) (prn-str payload') {:persistent true})))

(s/defmethod produce! :test
  [{:keys [topic payload]}
   {:keys [channel produced-messages]}]
  (let [payload' (assoc payload :meta {:correlation-id (-> (common-traceability/current-correlation-id)
                                                           common-traceability/correlation-id-appended)})]
    (lb/publish channel "" (topic->raw-topic topic) (prn-str payload') {:persistent true})
    (swap! produced-messages conj {:topic   topic
                                   :payload payload'})))

(defrecord ^:deprecated Producer [config]
  component/Lifecycle
  (start ^:deprecated [component]
    (let [config-content (:config config)
          uri (-> config-content :rabbitmq-uri)
          connection (rmq/connect {:uri uri})
          channel (lch/open connection)]
      (merge component {:rabbitmq-producer {:connection        connection
                                            :channel           channel
                                            :produced-messages (atom [])
                                            :current-env       (-> config :config :current-env)}})))

  (stop ^:deprecated [{:keys [rabbitmq-producer]}]
    (rmq/close (:channel rabbitmq-producer))
    (rmq/close (:connection rabbitmq-producer))))

(defn ^:deprecated new-producer []
  (->Producer {}))

(defrecord ^:deprecated MockProducer [config containers]
  component/Lifecycle
  (start ^:deprecated [component]
    (let [rabbitmq-container (-> containers :containers :rabbitmq)
          uri (.getAmqpUrl rabbitmq-container)
          connection (rmq/connect {:uri uri})
          channel (lch/open connection)]
      (merge component {:rabbitmq-producer {:connection        connection
                                            :channel           channel
                                            :produced-messages (atom [])
                                            :current-env       (-> config :config :current-env)}})))

  (stop ^:deprecated [{:keys [rabbitmq-producer]}]
    (rmq/close (:channel rabbitmq-producer))
    (rmq/close (:connection rabbitmq-producer))))

(defn ^:deprecated new-mock-producer []
  (->MockProducer {} {}))

