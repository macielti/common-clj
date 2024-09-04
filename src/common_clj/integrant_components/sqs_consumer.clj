(ns common-clj.integrant-components.sqs-consumer
  (:require [amazonica.aws.sqs :as sqs]
            [clojure.tools.reader.edn :as edn]
            [integrant.core :as ig]
            [schema.core :as s]
            [taoensso.timbre :as log])
  (:import (clojure.lang IFn)))

(s/defschema Consumers
  {s/Keyword {:schema     s/Any
              :handler-fn IFn}})

(s/defn create-sqs-queues!
  [aws-credentials :- (s/pred map?)
   queues :- s/Str]
  (doseq [queue queues]
    (sqs/create-queue aws-credentials :queue-name queue)))

(defmethod ig/init-key :common-clj.integrant-components.sqs-consumer/sqs-consumer
  [_ {:keys [components consumers]}]
  (log/info :starting :common-clj.integrant-components.sqs-consumer/sqs-consumer)
  (let [env (-> components :config :current-env)
        switch (atom true)
        aws-credential {:access-key (-> components :config env :aws-credentials :access-key)
                        :secret-key (-> components :config env :aws-credentials :secret-key)
                        :endpoint   (-> components :config env :aws-credentials :endpoint)}
        _ (create-sqs-queues! aws-credential (-> components :config env :queues))
        queues (mapv (fn [queue]
                       (-> (sqs/get-queue-url aws-credential queue)
                           (assoc :queue queue))) (-> components :config env :queues))]
    (doseq [{:keys [queue-url queue]} queues]
      (future
        (while @switch
          (let [{:keys [messages]} (sqs/receive-message aws-credential queue-url)]
            (doseq [message messages]
              (try
                (let [{:keys [handler-fn schema]} (get consumers queue)
                      message' {:payload (s/validate schema (edn/read-string (:body message)))
                                :queue   queue}]
                  (handler-fn {:message    message'
                               :components components})
                  (log/info :message-handled message')
                  (sqs/delete-message aws-credential (assoc message :queue-url queue-url)))
                (catch Exception ex
                  (log/error ex)))))
          (Thread/sleep 1000))))
    {:switch switch}))

(defmethod ig/halt-key! :common-clj.integrant-components.sqs-consumer/sqs-consumer
  [_ {:keys [switch]}]
  (log/info :stopping :common-clj.integrant-components.sqs-consumer/sqs-consumer)
  (reset! switch false))