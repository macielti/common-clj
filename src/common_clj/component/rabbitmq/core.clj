(ns common-clj.component.rabbitmq.core
  (:require [langohr.core :as rmq]
            [langohr.channel :as lch]
            [com.stuartsierra.component :as component]
            [langohr.queue :as lq]
            [langohr.basic :as lb]
            [schema.core :as s]
            [langohr.consumers :as lc]
            [cheshire.core :as json]
            [plumbing.core :as plumbing]))

(s/defschema Consumers
  {s/Keyword {:schema  s/Any
              :handler s/Any}})

(s/defschema Message
  {:topic s/Keyword
   :data  {:payload               {s/Keyword (s/maybe s/Any)}
           (s/optional-key :meta) {(s/optional-key :correlation-id) s/Str}}})

(s/defn topic->handler
  [topic :- s/Keyword
   consumers :- Consumers
   components]
  (let [{:keys [handler schema]} (get consumers topic)]
    (fn [_channel _meta ^bytes data]
      (let [{:keys [payload]} (-> (String. data "UTF-8")
                                  (json/parse-string true))]
        (s/validate schema payload)
        (handler payload components)))))

(s/defn ^:private load-consumers!
  [topics :- [s/Keyword]
   consumers :- Consumers
   channel
   components]
  (doseq [topic topics]
    (lc/subscribe channel (name topic) (topic->handler topic consumers components) {:auto-ack true})))

(defmulti produce!
  (fn [_message {:keys [current-env]}]
    current-env))

(s/defmethod produce! :prod
  [{:keys [topic data]} :- Message
   rabbitmq]
  (lb/publish (:channel rabbitmq) "" (name topic) (json/encode data)))

(s/defmethod produce! :test
  [{:keys [topic data] :as message} :- Message
   {:keys [produced-messages] :as rabbitmq}]
  (lb/publish (:channel rabbitmq) "" (name topic) (json/encode data))
  (swap! produced-messages conj message))

(s/defn ^:private create-queues
  [queue-topics :- [s/Str]
   channel]
  (doseq [topic queue-topics]
    (lq/declare channel topic {:exclusive false :auto-delete false})))

(defrecord RabbitMQ [config consumers]
  component/Lifecycle
  (start [this]
    (let [{:keys [topics rabbitmq current-env]} (:config config)
          connection (rmq/connect rabbitmq)
          channel (lch/open connection)
          components (plumbing/assoc-when {} :config (:config config))]

      (-> (map name topics)
          (create-queues channel))

      (load-consumers! topics consumers channel components)

      (assoc this :rabbitmq {:connection        connection
                             :current-env       current-env
                             :channel           channel
                             :produced-messages (atom [])})))

  (stop [{:keys [rabbitmq] :as this}]
    (rmq/close (:channel rabbitmq))
    (rmq/close (:connection rabbitmq))
    (assoc this :rabbitmq nil)))

(defn new-rabbitmq [consumers]
  (->RabbitMQ {} consumers))