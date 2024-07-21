(ns common-clj.component.rabbitmq.consumer
  (:require [clojure.tools.reader.edn :as edn]
            [com.stuartsierra.component :as component]
            [langohr.channel :as lch]
            [langohr.core :as rmq]
            [langohr.queue :as lq]
            [langohr.consumers :as lc]
            [medley.core :as medley]
            [schema.core :as s]
            [taoensso.timbre :as log]
            [common-clj.component.rabbitmq.producer :as component.rabbitmq.producer])
  (:import (clojure.lang IFn)))

(s/defschema Consumers
  {s/Keyword {:schema     s/Any
              :handler-fn IFn}})

(defrecord Consumer [config datomic datalevin postgresql http-client rabbitmq-producer consumers]
  component/Lifecycle
  (start [component]
    (let [config-content (:config config)
          topics (-> config-content :topics)
          uri (-> config-content :rabbitmq-uri)
          connection (rmq/connect {:uri uri})
          channel (lch/open connection)
          components (medley/assoc-some {:config (:config config)}
                                        :rabbitmq-producer (:rabbitmq-producer rabbitmq-producer)
                                        :datomic (:datomic datomic)
                                        :datalevin (:datalevin datalevin)
                                        :postgresql (:postgresql postgresql)
                                        :http-client (:http-client http-client))
          service-name (:service-name config-content)]

      (s/validate Consumers consumers)

      (doseq [raw-topic topics
              :let [topic (keyword raw-topic)
                    consumer (topic consumers)
                    handler-fn (:handler-fn consumer)
                    schema (:schema consumer)]]
        (lq/declare channel raw-topic {:exclusive false :auto-delete false})
        (lc/subscribe channel raw-topic
                      (fn [_channel _meta payload]
                        (try
                          (s/validate schema (edn/read-string (String. payload "UTF-8")))
                          (handler-fn {:components components
                                       :payload    (edn/read-string (String. payload "UTF-8"))})
                          (catch Exception e
                            (do (log/error e)
                                (when (-> components :config :dead-letter-queue-service-integration-enabled)
                                  (component.rabbitmq.producer/produce! {:topic   :create-dead-letter
                                                                         :payload {:service        service-name
                                                                                   :topic          topic
                                                                                   :exception-info (str e)
                                                                                   :payload        (edn/read-string (String. payload "UTF-8"))}}
                                                                        (:rabbitmq-producer components)))))))
                      {:auto-ack true}))

      (merge component {:rabbitmq-consumer {:connection connection
                                            :channel    channel}})))

  (stop [{:keys [rabbitmq-consumer]}]
    (rmq/close (:channel rabbitmq-consumer))
    (rmq/close (:connection rabbitmq-consumer))))

(defn new-consumer [consumers]
  (->Consumer {} {} {} {} {} {} consumers))
