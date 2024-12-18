(ns common-clj.integrant-components.sqs-producer
  (:require [amazonica.aws.sqs :as sqs]
            [clojure.tools.logging :as log]
            [common-clj.integrant-components.sqs-consumer :as component.sqs-consumer]
            [common-clj.traceability.core :as common-traceability]
            [integrant.core :as ig]
            [medley.core :as medley]
            [schema.core :as s]))

(defmulti produce!
  (fn [_ {:keys [current-env]}]
    current-env))

(s/defmethod produce! :prod
  [{:keys [queue payload]} _producer]
  (let [payload' (assoc payload :meta {:correlation-id (-> (common-traceability/current-correlation-id)
                                                           common-traceability/correlation-id-appended)})]
    (sqs/send-message :queue-url (:queue-url (sqs/get-queue-url queue)) :message-body (prn-str payload'))))

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

  (when (= (-> components :config :current-env) :prod)
    (component.sqs-consumer/create-sqs-queues! (-> components :config :queues keys))
    (try (sqs/list-queues)
         (catch Exception ex
           (log/error :invalid-credentials :exception ex)
           (throw ex))))

  (medley/assoc-some {:current-env (-> components :config :current-env)}
                     :produced-messages (when (= (-> components :config :current-env) :test)
                                          (atom []))))

(defmethod ig/halt-key! ::sqs-producer
  [_ _sqs-producer]
  (log/info :stopping ::sqs-producer))
