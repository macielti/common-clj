(ns common-clj.component.telegram.core
  (:require [schema.core :as s]
            [io.pedestal.interceptor :as interceptor]
            [com.stuartsierra.component :as component]
            [io.pedestal.interceptor.chain :as chain]
            [telegrambot-lib.core :as telegram-bot]
            [clostache.parser :as parser]
            [medley.core :as medley]
            [overtone.at-at :as at-at]
            [common-clj.component.telegram.adapters.update :as telegram.adapters.message]
            [common-clj.component.telegram.models.consumer :as component.telegram.models.consumer]))

(s/defn send-message!
  [message :- s/Str
   {:keys [telegram config]}]
  (telegram-bot/send-message telegram (-> config :telegram :chat-id) message))

(s/defn commit-update-as-consumed!
  [offset :- s/Int
   telegram]
  (telegram-bot/get-updates telegram {:offset (+ offset 1)}))

(s/defn interceptors-by-consumer
  [consumer
   {:keys [interceptors]}]
  (let [interceptor-groups (group-by :name interceptors)]
    (map #(-> (get interceptor-groups %) first) (:consumer/interceptors consumer))))

(s/defn consume-update!
  [update
   consumers :- component.telegram.models.consumer/Consumers
   {:keys [telegram config] :as components}]
  (let [{:consumer/keys [handler error-handler type] :as consumer} (telegram.adapters.message/update->consumer update consumers)
        update-id (-> update :update_id)
        context   {:update     update
                   :components components}]
    (when (and handler update update-id)
      (try
        (chain/execute context
                       (concat (interceptors-by-consumer consumer consumers)
                               [(interceptor/interceptor {:name  :handler-interceptor
                                                          :enter handler})]))
        (catch Exception e
          (if error-handler
            (error-handler e components)
            (send-message! (parser/render-resource
                             (format "%s/error_processing_message_command.mustache"
                                     (-> config :telegram :message-template-dir))) components)))))
    (when-not handler
      (send-message! (parser/render-resource
                       (format "%s/command_not_found.mustache"
                               (-> config :telegram :message-template-dir))) components))
    (commit-update-as-consumed! update-id telegram)))

(s/defn ^:private consumer-job!
  [consumers
   {:keys [telegram] :as components}]
  (when-let [updates (-> (telegram-bot/get-updates telegram) :result)]
    (doseq [update updates] (consume-update! update consumers components))))

(defrecord Telegram [config datomic consumers]
  component/Lifecycle
  (start [component]
    (let [{{:keys [telegram] :as config-content} :config} config
          bot        (telegram-bot/create (:token telegram))
          components (medley/assoc-some {}
                                        :datomic (:datomic datomic)
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
