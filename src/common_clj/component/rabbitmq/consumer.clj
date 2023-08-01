(ns common-clj.component.rabbitmq.consumer
  (:require [clojure.tools.reader.edn :as edn]
            [com.stuartsierra.component :as component]
            [langohr.channel :as lch]
            [langohr.core :as rmq]
            [langohr.queue :as lq]
            [langohr.consumers :as lc]
            [plumbing.core :as plumbing]
            [schema.core :as s])
  (:import (clojure.lang IFn)))

(s/defschema Consumers
  {s/Keyword {:handler-fn IFn}})

(defrecord Consumer [config datomic datalevin http-client rabbitmq-producer consumers]
  component/Lifecycle
  (start [component]
    (let [config-content (:config config)
          topics (-> config-content :topics)
          uri (-> config-content :rabbitmq-uri)
          connection (rmq/connect {:uri uri})
          channel (lch/open connection)
          components (plumbing/assoc-when {:config (:config config)}
                                          :rabbitmq-producer (:rabbitmq-producer rabbitmq-producer)
                                          :datomic (:datomic datomic)
                                          :datalevin (:datalevin datalevin)
                                          :http-client (:http-client http-client))]

      (s/validate Consumers consumers)

      (doseq [raw-topic topics
              :let [topic (keyword raw-topic)
                    consumer (topic consumers)
                    handler-fn (:handler-fn consumer)]]
        (lq/declare channel raw-topic {:exclusive false :auto-delete false})
        (lc/subscribe channel raw-topic
                      (fn [_channel _meta payload] (handler-fn {:components components
                                                                :payload    (edn/read-string (String. payload "UTF-8"))}))
                      {:auto-ack true}))

      (merge component {:rabbitmq-consumer {:connection connection
                                            :channel    channel}})))

  (stop [{:keys [rabbitmq-consumer]}]
    (rmq/close (:channel rabbitmq-consumer))
    (rmq/close (:connection rabbitmq-consumer))))

(defn new-consumer [consumers]
  (->Consumer {} {} {} {} {} consumers))
