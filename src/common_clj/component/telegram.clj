(ns common-clj.component.telegram
  (:require [schema.core :as s]
            [com.stuartsierra.component :as component]
            [telegrambot-lib.core :as telegram-bot]
            [clostache.parser :as parser]
            [medley.core :as medley]
            [overtone.at-at :as at-at]
            [clojure.string :as str]))


(s/defn ^:private message->command-type :- s/Keyword
  [message-text :- s/Str]
  (-> (re-find #"\S*" message-text)
      (str/replace #"\/" "")
      str/lower-case
      keyword))

(s/defn ^:private message->handler
  [message-text :- s/Str
   consumers]
  (let [command-type (message->command-type message-text)]
    (command-type consumers)))


(s/defn send-message!
  [message :- s/Str
   {:keys [telegram config]}]
  (telegram-bot/send-message telegram (-> config :telegram :chat-id) message))


(s/defn ^:private commit-update-as-consumed!
  [offset :- s/Int
   bot]
  (telegram-bot/get-updates bot {:offset (+ offset 1)}))

(s/defn ^:private consume-update!
  [update
   consumers
   {:keys [telegram] :as components}]
  (let [handler   (message->handler (-> update :message :text) consumers)
        message   (:message update)
        update-id (-> update :update_id)]
    (when (and handler message update-id)
      (handler message components))
    (when-not handler
      (send-message! (parser/render-resource "templates/command_not_found.mustache") components))
    (commit-update-as-consumed! update-id telegram)))


(s/defn ^:private consumer-job!
  [chat-id :- s/Int
   consumers
   {:keys [telegram] :as components}]
  (when-let [updates (-> (telegram-bot/get-updates telegram) :result)]
    (doseq [update updates] (consume-update! update consumers components))))

(defrecord Telegram [config database consumers]
  component/Lifecycle
  (start [component]
    (let [{{:keys [telegram] :as config-content} :config} config
          token      (:token telegram)
          chat-id    (:chat-id telegram)
          telegram   (telegram-bot/create token)
          components (medley/assoc-some {}
                                        :database (:database database)
                                        :config config-content
                                        :telegram telegram)
          pool       (at-at/mk-pool)]
      (at-at/interspaced 100 (partial consumer-job! chat-id consumers components) pool)
      (merge component {:telegram telegram})))

  (stop [{:keys [telegram]}]
    (telegram-bot/close telegram)))                         ;TODO: stop at-at pool here

(defn new-telegram
  [consumers]
  (map->Telegram {:consumers consumers}))
