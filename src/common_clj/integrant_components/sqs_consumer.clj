(ns common-clj.integrant-components.sqs-consumer
  (:require [amazonica.aws.sqs :as sqs]
            [clojure.tools.reader.edn :as edn]
            [integrant.core :as ig]
            [medley.core :as medley]
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
  [aws-credentials :- (s/pred map?)
   queues :- s/Str]
  (doseq [queue queues]
    (sqs/create-queue aws-credentials :queue-name queue)))

(defmulti consume!
  (fn [{:keys [current-env]}]
    current-env))

(s/defmethod consume! :prod
  [{:keys [aws-credentials switch components consumers]}]
  (create-sqs-queues! aws-credentials (-> components :config :queues))
  (let [queues (mapv (fn [queue]
                       (-> (sqs/get-queue-url aws-credentials queue)
                           (assoc :queue queue))) (-> components :config :queues))]
    (doseq [{:keys [queue-url queue]} queues]
      (future
        (try
          (while @switch
            (let [{:keys [messages]} (sqs/receive-message aws-credentials {:queue-url queue-url :wait-time-seconds 20})]
              (doseq [message messages]
                (try
                  (let [{:keys [handler-fn schema]} (get consumers queue)
                        message' {:payload (s/validate schema (edn/read-string (:body message)))
                                  :queue   queue}]
                    (handler-fn {:message    message'
                                 :components components})
                    (log/info :message-handled message')
                    (sqs/delete-message aws-credentials (assoc message :queue-url queue-url)))
                  (catch Exception ex-in
                    (log/error ex-in))))))
          (catch Exception ex-ext
            (log/error ex-ext)))))))

(s/defmethod consume! :test
  [{:keys [switch components consumers consumed-messages produced-messages]}]
  (let [queues (-> components :config :queues)]
    (doseq [queue queues]
      (future
        (while @switch
          (let [messages (fetch-messages-waiting-to-be-processed! queue produced-messages consumed-messages)]
            (doseq [message messages]
              (try
                (let [{:keys [handler-fn schema]} (get consumers queue)]

                  (s/validate schema (-> message :payload (dissoc message :meta)))

                  (handler-fn {:message    (-> message :payload (dissoc message :meta))
                               :components components})

                  (log/info :message-handled message)

                  (commit-message-as-consumed! message consumed-messages))
                (catch Exception ex
                  (log/error ex)))))
          (Thread/sleep 10))))))

(defmethod ig/init-key ::sqs-consumer
  [_ {:keys [components consumers]}]
  (log/info :starting ::sqs-consumer)
  (let [switch (atom true)
        aws-credentials {:access-key (-> components :config :aws-credentials :access-key)
                         :secret-key (-> components :config :aws-credentials :secret-key)
                         :endpoint   (-> components :config :aws-credentials :endpoint)}]

    (consume! (medley/assoc-some {:switch          switch
                                  :aws-credentials aws-credentials
                                  :consumers       consumers
                                  :components      components
                                  :current-env     (-> components :config :current-env)}
                                 :produced-messages (when (= (-> components :config :current-env) :test)
                                                      (-> components :producer :produced-messages))
                                 :consumed-messages (when (= (-> components :config :current-env) :test)
                                                      (atom []))))
    {:switch switch}))

(defmethod ig/halt-key! ::sqs-consumer
  [_ {:keys [switch]}]
  (log/info :stopping ::sqs-consumer)
  (reset! switch false))
