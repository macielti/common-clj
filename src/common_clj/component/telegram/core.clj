(ns common-clj.component.telegram.core
  (:require [schema.core :as s]
            [com.stuartsierra.component :as component]
            [telegrambot-lib.core :as telegram-bot]
            [clostache.parser :as parser]
            [medley.core :as medley]
            [overtone.at-at :as at-at]
            [common-clj.component.telegram.adapters.message :as telegram.adapters.message]))

(s/defn send-message!
  [message :- s/Str
   {:keys [telegram config]}]
  (telegram-bot/send-message telegram (-> config :telegram :chat-id) message))

(s/defn commit-update-as-consumed!
  [offset :- s/Int
   telegram]
  (telegram-bot/get-updates telegram {:offset (+ offset 1)}))

(s/defn consume-update!
  [update
   consumers
   {:keys [telegram] :as components}]
  (let [{:consumer/keys [handler error-handler]} (telegram.adapters.message/message->handler (-> update :message :text) consumers)
        message   (:message update)
        update-id (-> update :update_id)]
    (when (and handler message update-id)
      (try
        (handler message components)
        (catch Exception e
          (when error-handler
            (error-handler e components)))))
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
