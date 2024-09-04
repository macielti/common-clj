(ns common-clj.integrant-components.sqs-producer
  (:require [amazonica.aws.sqs :as sqs]
            [integrant.core :as ig]
            [taoensso.timbre :as log]))

(defmethod ig/init-key :common-clj.integrant-components.sqs-producer/sqs-producer
  [_ {:keys [components]}]
  (log/info :starting :common-clj.integrant-components.sqs-producer/sqs-producer)
  (let [aws-credentials {:access-key (-> components :config :aws-credentials :access-key)
                         :secret-key (-> components :config :aws-credentials :secret-key)
                         :endpoint   (-> components :config :aws-credentials :endpoint)}]

    (try (sqs/list-queues aws-credentials)
         (catch Exception ex
           (log/error :invalid-credentials :exception ex)
           (throw ex)))

    {:aws-credential aws-credentials}))

(defmethod ig/halt-key! :common-clj.integrant-components.sqs-producer/sqs-producer
  [_ _sqs-producer]
  (log/info :stopping :common-clj.integrant-components.sqs-producer/sqs-producer))
