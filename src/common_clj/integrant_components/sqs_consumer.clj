(ns common-clj.integrant-components.sqs-consumer
  (:require [amazonica.aws.sqs :as sqs]
            [clojure.tools.reader.edn :as edn]
            [common-clj.traceability.core :as common-traceability]
            [integrant.core :as ig]
            [medley.core :as medley]
            [schema.core :as s]
            [taoensso.timbre :as log]
            [parallel.core :as p])
  (:import (clojure.lang IFn)))

(s/defschema Consumers
  {s/Keyword {:schema     s/Any
              :handler-fn IFn}})

(defn commit-message-as-consumed!
  [message
   consumed-messages]
  (swap! consumed-messages conj message))

(s/defn fetch-messages-waiting-to-be-processed!
  [queue :- s/Str
   produced-messages
   consumed-messages]
  (-> (filterv #(= queue (:queue %)) @produced-messages)
      set
      (clojure.set/difference (set @consumed-messages))))

(s/defn create-sqs-queues!
  [queues :- [s/Str]]
  (doseq [queue queues]
    (sqs/create-queue :queue-name queue)))

(defmulti consume!
  (fn [{:keys [current-env]}]
    current-env))

(s/defmethod consume! :prod
  [{:keys [switch components consumers]}
   consumer-parallelism :- s/Int]
  (create-sqs-queues! (-> components :config :queues))
  (let [queues (mapv (fn [queue]
                       (-> (sqs/get-queue-url queue)
                           (assoc :queue queue))) (-> components :config :queues))]
    (doseq [{:keys [queue-url queue]} queues]
      (future
        (try
          (while @switch
            (let [{:keys [messages]} (sqs/receive-message :queue-url queue-url :wait-time-seconds 20)]
              (p/pmap (fn parallel-message-consumption
                        [message]
                        (try
                          (let [{:keys [handler-fn schema]} (get consumers queue)
                                message' (-> message :body edn/read-string)]
                            (binding [common-traceability/*correlation-id* (-> message'
                                                                               :meta
                                                                               :correlation-id
                                                                               common-traceability/correlation-id-appended)]
                              (try
                                (handler-fn {:message    (s/validate schema (dissoc message' :meta))
                                             :components components})
                                (log/debug ::message-handled {:queue   queue
                                                              :message (dissoc message :body)})
                                (sqs/delete-message (assoc message :queue-url queue-url))
                                (catch Exception ex-handling-message
                                  (log/error ::exception-while-handling-aws-sqs-message ex-handling-message)))))
                          (catch Exception ex-in
                            (log/error ex-in)))) messages consumer-parallelism)))
          (catch Exception ex-ext
            (log/error ex-ext)))))))

(s/defmethod consume! :test
  [{:keys [switch components consumers consumed-messages produced-messages]}
   _consumer-parallelism :- s/Int]
  (let [queues (-> components :config :queues)]
    (doseq [queue queues]
      (future
        (while @switch
          (let [messages (fetch-messages-waiting-to-be-processed! queue produced-messages consumed-messages)]
            (doseq [message messages]
              (binding [common-traceability/*correlation-id* (-> message
                                                                 :payload
                                                                 :meta
                                                                 :correlation-id
                                                                 common-traceability/correlation-id-appended)]
                (try
                  (let [{:keys [handler-fn schema]} (get consumers queue)]

                    (s/validate schema (-> message :payload (dissoc message :meta)))

                    (handler-fn {:message    (-> message :payload (dissoc message :meta))
                                 :components components})

                    (log/debug ::message-handled message)

                    (commit-message-as-consumed! message consumed-messages))
                  (catch Exception ex
                    (log/error ex))))))
          (Thread/sleep 10))))))

(defmethod ig/init-key ::sqs-consumer
  [_ {:keys [components consumers]}]
  (log/info :starting ::sqs-consumer)
  (let [switch (atom true)]

    (consume! (medley/assoc-some {:switch      switch
                                  :consumers   consumers
                                  :components  components
                                  :current-env (-> components :config :current-env)}
                                 :produced-messages (when (= (-> components :config :current-env) :test)
                                                      (-> components :producer :produced-messages))
                                 :consumed-messages (when (= (-> components :config :current-env) :test)
                                                      (atom [])))
              (get-in components [:config :consumer-parallelism] 4))
    {:switch switch}))

(defmethod ig/halt-key! ::sqs-consumer
  [_ {:keys [switch]}]
  (log/info :stopping ::sqs-consumer)
  (reset! switch false))
