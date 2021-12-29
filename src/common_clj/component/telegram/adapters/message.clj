(ns common-clj.component.telegram.adapters.message
  (:require [schema.core :as s]
            [clojure.string :as str]
            [common-clj.component.telegram.models.consumer :as component.telegram.models.consumer]
            [taoensso.timbre :as timbre]))

(s/defn message->consumer-key :- s/Keyword
  [message-text :- s/Str]
  (-> (re-find #"\S*" message-text)
      (str/replace #"\/" "")
      str/lower-case
      keyword))

(s/defn update->consumer
  [{:keys [message callback_query]}
   consumers :- component.telegram.models.consumer/Consumers]
  (let [consumer-type (cond
                        message :message
                        callback_query :callback-query)
        consumer-key  (message->consumer-key (:text message))]
    (-> (get consumers consumer-type)
        (get consumer-key)
        (assoc :consumer/type consumer-type))))
