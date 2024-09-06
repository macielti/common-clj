(ns common-clj.integrant-components.sqs-producer
  (:require [amazonica.aws.sqs :as sqs]
            [common-clj.traceability.core :as common-traceability]
            [integrant.core :as ig]
            [medley.core :as medley]
            [schema.core :as s]
            [taoensso.timbre :as log]))

(defmulti produce!
  (fn [_ {:keys [current-env]}]
    current-env))

(s/defmethod produce! :prod
  [{:keys [queue payload]}
   {:keys [aws-credentials]}]
  (let [payload' (assoc payload :meta {:correlation-id (-> (common-traceability/current-correlation-id)
                                                           common-traceability/correlation-id-appended)})
        queue-url (-> (sqs/get-queue-url aws-credentials queue) :queue-url)]
    (sqs/send-message aws-credentials {:queue-url    queue-url
                                       :message-body (prn-str payload')})))

(s/defmethod produce! :test
  [{:keys [queue payload]}
   {:keys [produced-messages]}]
  (let [payload' (assoc payload :meta {:correlation-id (-> (common-traceability/current-correlation-id)
                                                           common-traceability/correlation-id-appended)})]
    (swap! produced-messages conj {:queue   queue
                                   :payload payload'})))

(defmethod ig/init-key ::sqs-producer
  [_ {:keys [components]}]
  (log/info :starting ::sqs-producer)
  (let [aws-credentials {:access-key (-> components :config :aws-credentials :access-key)
                         :secret-key (-> components :config :aws-credentials :secret-key)
                         :endpoint   (-> components :config :aws-credentials :endpoint)}]

    (when (= (-> components :config :current-env) :prod)
      (try (sqs/list-queues aws-credentials)
           (catch Exception ex
             (log/error :invalid-credentials :exception ex)
             (throw ex))))

    (medley/assoc-some {:current-env     (-> components :config :current-env)
                        :aws-credentials aws-credentials}
                       :produced-messages (when (= (-> components :config :current-env) :test)
                                            (atom [])))))

(defmethod ig/halt-key! ::sqs-producer
  [_ _sqs-producer]
  (log/info :stopping ::sqs-producer))
