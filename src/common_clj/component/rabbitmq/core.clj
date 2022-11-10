(ns common-clj.component.rabbitmq.core
  (:require [langohr.core :as rmq]
            [langohr.channel :as lch]
            [com.stuartsierra.component :as component]
            [langohr.queue :as lq]
            [langohr.basic :as lb]
            [schema.core :as s]
            [langohr.consumers :as lc]
            [cheshire.core :as json]))

(s/defschema Consumers
  {s/Keyword {:schema  s/Any
              :handler s/Any}})

(s/defschema Message
  {:topic s/Keyword
   :data  {:payload               {s/Keyword (s/maybe s/Any)}
           (s/optional-key :meta) {(s/optional-key :correlation-id) s/Str}}})

(s/defn ^:private load-consumers!
  [topics :- [s/Keyword]
   consumers :- Consumers
   channel]
  (doseq [topic topics]
    (lc/subscribe channel (name topic) (-> (get consumers topic) :handler) {:auto-ack true})))

(s/defn produce!
  [{:keys [topic data]} :- Message
   rabbitmq]
  (lb/publish (:channel rabbitmq) "" (name topic) (json/encode data)))

(s/defn ^:private create-queues
  [queue-topics :- [s/Str]
   channel]
  (doseq [topic queue-topics]
    (lq/declare channel topic {:exclusive false :auto-delete false})))

(defrecord RabbitMQ [config consumers]
  component/Lifecycle
  (start [this]
    (let [{:keys [topics]} (:config config)
          connection (rmq/connect)
          channel (lch/open connection)]

      (-> (map name topics)
          (create-queues channel))

      (load-consumers! topics consumers channel)

      (assoc this :rabbitmq {:connection connection
                             :channel    channel})))

  (stop [{:keys [rabbitmq] :as this}]
    (rmq/close (:channel rabbitmq))
    (rmq/close (:connection rabbitmq))
    (assoc this :rabbitmq nil)))

(defn new-rabbitmq [consumers]
  (->RabbitMQ {} consumers))