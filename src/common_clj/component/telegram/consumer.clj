(ns common-clj.component.telegram.consumer
  (:require [clostache.parser :as parser]
            [com.stuartsierra.component :as component]
            [common-clj.component.telegram.adapters.update :as telegram.adapters.message]
            [common-clj.component.telegram.models.consumer :as component.telegram.models.consumer]
            [common-clj.component.telegram.producer :as component.telegram.producer]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.interceptor.chain :as chain]
            [medley.core :as medley]
            [overtone.at-at :as at-at]
            [schema.core :as s]
            [telegrambot-lib.core :as telegram-bot]))

(s/defn commit-update-as-consumed!
  [offset :- s/Int
   telegram-bot]
  (telegram-bot/get-updates telegram-bot {:offset (+ offset 1)}))

(s/defn interceptors-by-consumer
  [consumer
   {:keys [interceptors]}]
  (let [interceptor-groups (group-by :name interceptors)]
    (map #(-> (get interceptor-groups %) first) (:consumer/interceptors consumer))))

(s/defn consume-update!
  [update
   consumers :- component.telegram.models.consumer/Consumers
   {:keys [telegram-consumer config] :as components}]
  (let [{:consumer/keys [handler error-handler] :as consumer} (telegram.adapters.message/update->consumer update consumers)
        token (-> config :telegram :token)
        update-id (-> update :update_id)
        chat-id (-> update :message :chat :id)
        context {:update     update
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
            (component.telegram.producer/produce! chat-id (parser/render-resource
                                                            (format "%s/error_processing_message_command.mustache"
                                                                    (-> config :telegram :message-template-dir))) token)))))
    (when-not handler
      (component.telegram.producer/produce! chat-id (parser/render-resource
                                                      (format "%s/command_not_found.mustache"
                                                              (-> config :telegram :message-template-dir))) token))
    (commit-update-as-consumed! update-id telegram-consumer)))

(s/defn ^:private consumer-job!
  [consumers
   {:keys [telegram-consumer] :as components}]
  (when-let [updates (-> (telegram-bot/get-updates telegram-consumer) :result)]
    (doseq [update updates]
      (consume-update! update consumers components))))

(defrecord TelegramConsumer [config datomic consumers]
  component/Lifecycle
  (start [component]
    (let [{{:keys [telegram] :as config-content} :config} config
          bot (telegram-bot/create (:token telegram))
          components (medley/assoc-some {}
                                        :datomic (:datomic datomic)
                                        :config config-content
                                        :telegram-consumer bot)
          pool (at-at/mk-pool)]
      (at-at/interspaced 100 (partial consumer-job! consumers components) pool)
      (assoc component :telegram-consumer {:bot    bot
                                           :poller pool})))

  (stop [{:keys [telegram-consumer]}]
    (telegram-bot/close (:bot telegram-consumer))
    (at-at/stop-and-reset-pool! (:poller telegram-consumer))))

(defn new-telegram-consumer
  [consumers]
  (map->TelegramConsumer {:consumers consumers}))


