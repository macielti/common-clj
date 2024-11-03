(ns common-clj.component.rabbitmq.consumer
  (:require [clojure.tools.reader.edn :as edn]
            [com.stuartsierra.component :as component]
            [common-clj.component.rabbitmq.producer :as component.rabbitmq.producer]
            [common-clj.traceability.core :as common-traceability]
            [langohr.channel :as lch]
            [langohr.consumers :as lc]
            [langohr.core :as rmq]
            [langohr.queue :as lq]
            [medley.core :as medley]
            [schema.core :as s]
            [taoensso.timbre :as log])
  (:import (clojure.lang IFn)))

(s/defschema ^:deprecated Consumers
  {s/Keyword {:schema     s/Any
              :handler-fn IFn}})

(defrecord ^:deprecated Consumer [config http-client prometheus rabbitmq-producer consumers]
  component/Lifecycle
  (start ^:deprecated [component]
    (let [config-content (:config config)
          topics (-> config-content :topics)
          uri (-> config-content :rabbitmq-uri)
          connection (rmq/connect {:uri uri})
          channel (lch/open connection)
          components (medley/assoc-some {:config (:config config)}
                                        :rabbitmq-producer (:rabbitmq-producer rabbitmq-producer)
                                        :http-client (:http-client http-client)
                                        :prometheus (:prometheus prometheus))
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
                          (s/validate schema (-> (String. payload "UTF-8") edn/read-string (dissoc :meta)))
                          (binding [common-traceability/*correlation-id* (-> (edn/read-string (String. payload "UTF-8"))
                                                                             :meta
                                                                             :correlation-id
                                                                             common-traceability/correlation-id-appended)]
                            (handler-fn {:components components
                                         :payload    (edn/read-string (String. payload "UTF-8"))}))
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

  (stop ^:deprecated [{:keys [rabbitmq-consumer]}]
    (rmq/close (:channel rabbitmq-consumer))
    (rmq/close (:connection rabbitmq-consumer))))

(defn ^:deprecated new-consumer [consumers]
  (->Consumer {} {} {} {} consumers))

(defrecord ^:deprecated MockConsumer [config http-client prometheus rabbitmq-producer containers consumers]
  component/Lifecycle
  (start ^:deprecated [component]
    (let [rabbitmq-container (-> containers :containers :rabbitmq)
          config-content (:config config)
          topics (-> config-content :topics)
          uri (.getAmqpUrl rabbitmq-container)
          connection (rmq/connect {:uri uri})
          channel (lch/open connection)
          components (medley/assoc-some {:config (:config config)}
                                        :rabbitmq-producer (:rabbitmq-producer rabbitmq-producer)
                                        :http-client (:http-client http-client)
                                        :prometheus (:prometheus prometheus))
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
                          (s/validate schema (-> (String. payload "UTF-8") edn/read-string (dissoc :meta)))
                          (binding [common-traceability/*correlation-id* (-> (edn/read-string (String. payload "UTF-8"))
                                                                             :meta
                                                                             :correlation-id
                                                                             common-traceability/correlation-id-appended)]
                            (handler-fn {:components components
                                         :payload    (edn/read-string (String. payload "UTF-8"))}))
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

  (stop ^:deprecated [component]
    component))

(defn ^:deprecated new-mock-rabbitmq-consumer [consumers]
  (->MockConsumer {} {} {} {} {} consumers))
