(ns common-clj.component.telegram.adapters.update
  (:require [schema.core :as s]
            [clojure.string :as str]
            [common-clj.component.telegram.models.consumer :as component.telegram.models.consumer]
            [taoensso.timbre :as timbre]
            [cheshire.core :as json]))

(defmulti update->consumer-key
          (s/fn [_
                 consumer-type :- s/Keyword]
            consumer-type))

(s/defmethod update->consumer-key :message :- s/Keyword
             [{:keys [message]}
              _]
             (let [{:keys [text]} message]
               (-> (re-find #"\S*" text)
                   (str/replace #"\/" "")
                   str/lower-case
                   keyword)))

(s/defmethod update->consumer-key :callback-query :- s/Keyword
             [{:keys [callback_query]}
              _]
             (let [{:keys [data]} callback_query]
               (some-> (try (json/parse-string data true)
                            (catch Exception _ nil))
                       :handler
                       keyword)))

(s/defn update->consumer
  [{:keys [message callback_query] :as update}
   consumers :- component.telegram.models.consumer/Consumers]
  (let [consumer-type (cond
                        message :message
                        callback_query :callback-query)
        consumer-key  (update->consumer-key update consumer-type)]
    (some-> (get consumers consumer-type)
            (get consumer-key)
            (assoc :consumer/type consumer-type))))

(s/defn update->chat-id :- s/Int
  [update]
  (or (some-> update :message :chat :id)
      (some-> update :callback_query :message :chat :id)))
