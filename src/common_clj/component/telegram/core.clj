(ns common-clj.component.telegram.core
  (:require [schema.core :as s]
            [com.stuartsierra.component :as component]
            [io.pedestal.interceptor.chain :as chain]
            [telegrambot-lib.core :as telegram-bot]
            [clostache.parser :as parser]
            [medley.core :as medley]
            [overtone.at-at :as at-at]
            [common-clj.component.telegram.adapters.message :as telegram.adapters.message]
            [common-clj.component.telegram.models.consumer :as component.telegram.models.consumer]
            [taoensso.timbre :as timbre]
            [io.pedestal.interceptor :as interceptor])
  (:import (io.pedestal.interceptor Interceptor)))

(s/defn send-message!
  [message :- s/Str
   {:keys [telegram config]}]
  (telegram-bot/send-message telegram (-> config :telegram :chat-id) message))

(s/defn commit-update-as-consumed!
  [offset :- s/Int
   telegram]
  (telegram-bot/get-updates telegram {:offset (+ offset 1)}))

(s/defn interceptors-by-consumer :- [Interceptor]
  [consumer :- component.telegram.models.consumer/Consumer
   consumers :- component.telegram.models.consumer/Consumers]
  (let [interceptor-groups (group-by :name (:interceptors consumers))]
    (map #(-> (get interceptor-groups %) first) (:consumer/interceptors consumer))))

(s/defn consume-update!
  [update
   consumers :- component.telegram.models.consumer/Consumers
   {:keys [telegram] :as components}]
  (let [{:consumer/keys [handler error-handler] :as consumer} (telegram.adapters.message/message->handler (-> update :message :text) consumers)
        message   (:message update)
        update-id (-> update :update_id)
        context   {:message    message
                   :components components}]
    (when (and handler message update-id)
      (try
        (chain/execute context
                       (concat (interceptors-by-consumer consumer consumers)
                               [(interceptor/interceptor {:name  :handler-interceptor
                                                          :enter handler})]))
        (catch Exception e
          (if error-handler
            (error-handler e components)
            (send-message! (parser/render-resource "templates/error_processing_message_command.mustache") components)))))
    (when-not handler
      (send-message! (parser/render-resource "templates/command_not_found.mustache") components))
    (commit-update-as-consumed! update-id telegram)))

(s/defn ^:private consumer-job!
  [consumers
   {:keys [telegram] :as components}]
  (when-let [updates (-> (telegram-bot/get-updates telegram) :result)]
    (doseq [update updates] (consume-update! update consumers components))))

(defrecord Telegram [config database consumers]
  component/Lifecycle
  (start [component]
    (let [{{:keys [telegram] :as config-content} :config} config
          bot        (telegram-bot/create (:token telegram))
          components (medley/assoc-some {}
                                        :database (:database database)
                                        :config config-content
                                        :telegram bot)
          pool       (at-at/mk-pool)]
      (at-at/interspaced 100 (partial consumer-job! consumers components) pool)
      (merge component {:telegram bot})))

  (stop [{:keys [telegram]}]
    (telegram-bot/close telegram)))                         ;TODO: stop at-at pool here

(defn new-telegram
  [consumers]
  (map->Telegram {:consumers consumers}))
