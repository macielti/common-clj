(ns common-clj.component.telegram.adapters.update
  (:require [clojure.string :as string]
            [common-clj.component.telegram.models.consumer :as component.telegram.models.consumer]
            [common-clj.component.telegram.models.update :as component.telegram.models.update]
            [medley.core :as medley]
            [schema.core :as s]
            [taoensso.timbre :as log]))

(s/defn wire-update->file-id :- (s/maybe s/Str)
  [update]
  (cond
    (some-> update :message :photo last :file_id)
    (-> update :message :photo last :file_id)))

(s/defn wire-update->type :- s/Keyword
  [update]
  (cond
    (or (= (-> update :message :entities first :type) "bot_command")
        (= (-> update :message :caption_entities first :type) "bot_command")
        (= (-> update :channel_post :entities first :type) "bot_command")
        (:callback_query update)) :bot-command
    :else :others))

(s/defn update->chat-id :- s/Int
  [update]
  (or (-> update :message :chat :id)
      (-> update :edited_message :chat :id)
      (-> update :callback_query :message :chat :id)
      (-> update :my_chat_member :chat :id)
      (-> update :channel_post :chat :id)))

(s/defn update->message-content :- s/Str
  [update]
  (or (-> update :message :caption)
      (-> update :message :text)
      (-> update :channel_post :text)
      (-> update :callback_query :data)))

(s/defn update->user :- component.telegram.models.update/User
  [update]
  (let [from (or (some-> update :message :from)
                 (some-> update :callback_query :from))]
    (medley/assoc-some {:user/id (-> from :id)}
                       :user/username (some-> from :username)
                       :user/first-name (some-> from :first_name)
                       :user/last-name (some-> from :last_name))))

(s/defn wire->internal :- component.telegram.models.update/Update
  [{:keys [update_id] :as update}]
  (let [file-id (wire-update->file-id update)
        user (update->user update)]
    (medley/assoc-some {:update/id      update_id
                        :update/chat-id (update->chat-id update)
                        :update/type    (wire-update->type update)
                        :update/message (update->message-content update)}
                       :update/user (when user
                                      user)
                       :update/file-id file-id)))

(defmulti update->consumer-key
  (s/fn [_
         consumer-type :- s/Keyword]
    consumer-type))

(s/defmethod update->consumer-key :bot-command :- s/Keyword
  [{:update/keys [message]} :- component.telegram.models.update/Update
   _]
  (-> (string/split message #" ")
      first
      (string/replace #"\/" "")
      string/lower-case
      keyword))

(s/defmethod update->consumer-key :others :- s/Keyword
  [update
   _]
  (log/info :unsupported-update-type update)
  :others)

(s/defn update->consumer
  [{:update/keys [type] :as update} :- component.telegram.models.update/Update
   consumers :- component.telegram.models.consumer/Consumers]
  (let [consumer-key (update->consumer-key update type)]
    (when-not (= type :others)
      (some-> (get consumers type)
              (get consumer-key)
              (assoc :consumer/type type)))))
