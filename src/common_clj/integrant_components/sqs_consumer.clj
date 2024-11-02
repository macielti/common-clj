(ns common-clj.integrant-components.sqs-consumer
  (:require [amazonica.aws.sqs :as sqs]
            [clojure.set]
            [clojure.tools.reader.edn :as edn]
            [common-clj.traceability.core :as common-traceability]
            [diehard.core :as dh]
            [integrant.core :as ig]
            [medley.core :as medley]
            [overtone.at-at :as at-at]
            [schema.core :as s]
            [taoensso.timbre :as log])
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
  [{:keys [switch components queue consumer]}]
  (let [queue-url (-> (sqs/get-queue-url queue) :queue-url)]
    (try
      (while @switch
        (let [{:keys [messages]} (sqs/receive-message :queue-url queue-url :wait-time-seconds 20)]
          (doseq [message messages]
            (try
              (let [message' (-> message :body edn/read-string)]
                (binding [common-traceability/*correlation-id* (-> message'
                                                                   :meta
                                                                   :correlation-id
                                                                   common-traceability/correlation-id-appended)]
                  (try
                    (dh/with-timeout {:timeout-ms (get-in components [:config :message-consumption-timeout-ms] 30000)
                                      :interrupt? true}
                      ((:handler-fn consumer) {:message    (s/validate (:schema consumer) (dissoc message' :meta))
                                               :components components}))
                    (log/debug ::message-handled {:queue   queue
                                                  :message (dissoc message :body)})
                    (sqs/delete-message (assoc message :queue-url queue-url))
                    (catch Exception ex-handling-message
                      (log/error ::exception-while-handling-aws-sqs-message ex-handling-message)))))
              (catch Exception ex-in
                (log/error ex-in))))))
      (catch Exception ex-ext
        (log/error ex-ext)))))

(s/defmethod consume! :test
  [{:keys [switch components consumed-messages produced-messages queue consumer]}]
  (while @switch
    (let [messages (fetch-messages-waiting-to-be-processed! queue produced-messages consumed-messages)]
      (doseq [message messages]
        (binding [common-traceability/*correlation-id* (-> message
                                                           :payload
                                                           :meta
                                                           :correlation-id
                                                           common-traceability/correlation-id-appended)]
          (try
            (s/validate (:schema consumer) (-> message :payload (dissoc message :meta)))

            ((:handler-fn consumer) {:message    (-> message :payload (dissoc message :meta))
                                     :components components})

            (log/debug ::message-handled message)

            (commit-message-as-consumed! message consumed-messages)
            (catch Exception ex
              (log/error ex))))))
    (Thread/sleep 10)))

(defn ensure-consumers-threads-are-up!
  [consumers-thread-pool
   switch
   consumers
   components]
  (doseq [queue (keys @consumers-thread-pool)]
    (if (< (->> (get @consumers-thread-pool queue) (filter #(not (future-done? %))) count)
           (get-in components [:config :queues queue :parallel-consumers] 4))
      (do
        (log/warn ::consumers-threads-count-bellow-treshold :starting-new-consumer-thread :queue queue)
        (swap! consumers-thread-pool
               update queue conj (future (consume! (medley/assoc-some {:switch      switch
                                                                       :consumer    (get consumers queue)
                                                                       :queue       queue
                                                                       :components  components
                                                                       :current-env (-> components :config :current-env)}
                                                                      :produced-messages (when (= (-> components :config :current-env) :test)
                                                                                           (-> components :producer :produced-messages))
                                                                      :consumed-messages (when (= (-> components :config :current-env) :test)
                                                                                           (atom [])))))))
      (log/debug ::all-expected-consumers-threads-are-up-and-running :queue queue))))

(defmethod ig/init-key ::sqs-consumer
  [_ {:keys [components consumers]}]
  (log/info :starting ::sqs-consumer)
  (let [switch (atom true)
        queues (-> components :config :queues keys)
        consumers-thread-pool (atom (reduce (fn [acc cur]
                                              (assoc acc cur [])) {} queues))
        pool (at-at/mk-pool)]

    (when (= (-> components :config :current-env) :prod)
      (create-sqs-queues! queues))

    (doseq [queue queues]
      (dotimes [thread-number (get-in components [:config :queues queue :parallel-consumers] 4)]
        (log/info ::starting-consumer-thread thread-number :queue queue)
        (swap! consumers-thread-pool
               update queue conj (future (consume! (medley/assoc-some {:switch      switch
                                                                       :consumer    (get consumers queue)
                                                                       :queue       queue
                                                                       :components  components
                                                                       :current-env (-> components :config :current-env)}
                                                                      :produced-messages (when (= (-> components :config :current-env) :test)
                                                                                           (-> components :producer :produced-messages))
                                                                      :consumed-messages (when (= (-> components :config :current-env) :test)
                                                                                           (atom []))))))))

    (at-at/interspaced 1000 #(try (ensure-consumers-threads-are-up! consumers-thread-pool switch consumers components)
                                  (catch Exception ex
                                    (log/error ex))) pool)

    {:switch switch}))

(defmethod ig/halt-key! ::sqs-consumer
  [_ {:keys [switch]}]
  (log/info :stopping ::sqs-consumer)
  (reset! switch false))
