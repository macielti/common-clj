(ns common-clj.component.rabbitmq.producer
  (:require [com.stuartsierra.component :as component]
            [langohr.core :as rmq]
            [langohr.channel :as lch]
            [langohr.basic :as lb]
            [schema.core :as s]))

(s/defn topic->raw-topic :- s/Str
  [topic :- s/Keyword]
  (let [raw-topic-namespace (namespace topic)
        raw-topic-keyword (name topic)]
    (if raw-topic-namespace
      (str raw-topic-namespace "/" raw-topic-keyword)
      raw-topic-keyword)))

(defmulti produce!
  (fn [_ {:keys [current-env]}]
    current-env))

(s/defmethod produce! :prod
  [{:keys [topic payload]}
   {:keys [channel]}]
  (lb/publish channel "" (topic->raw-topic topic) (prn-str payload)))

(s/defmethod produce! :test
  [{:keys [topic payload]}
   {:keys [channel produced-messages]}]
  (lb/publish channel "" (topic->raw-topic topic) (prn-str payload))
  (swap! produced-messages conj {:topic   topic
                                 :payload payload}))

(defrecord Producer [config]
  component/Lifecycle
  (start [component]
    (let [config-content (:config config)
          uri (-> config-content :rabbitmq-uri)
          connection (rmq/connect {:uri uri})
          channel (lch/open connection)]
      (merge component {:rabbitmq-producer {:connection        connection
                                            :channel           channel
                                            :produced-messages (atom [])
                                            :current-env       (-> config :config :current-env)}})))

  (stop [{:keys [rabbitmq-producer]}]
    (rmq/close (:channel rabbitmq-producer))
    (rmq/close (:connection rabbitmq-producer))))

(defn new-producer []
  (map->Producer {}))

