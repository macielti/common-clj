(ns common-clj.component.telegram.consumer
  (:require [schema.core :as s]
            [medley.core :as medley]
            [telegrambot-lib.core :as telegram-bot]
            [clostache.parser :as parser]
            [overtone.at-at :as at-at]
            [com.stuartsierra.component :as component]
            [common-clj.component.telegram.producer :as component.telegram.producer]
            [common-clj.component.telegram.models.consumer :as component.telegram.models.consumer]
            [common-clj.component.telegram.adapters.update :as telegram.adapters.message]
            [io.pedestal.interceptor.chain :as chain]
            [io.pedestal.interceptor :as interceptor]))

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
  (let [{:consumer/keys [handler error-handler type] :as consumer} (telegram.adapters.message/update->consumer update consumers)
        {{:keys [token]} :telegram} config
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
            (component.telegram.producer/produce! (parser/render-resource
                                                    (format "%s/error_processing_message_command.mustache"
                                                            (-> config :telegram :message-template-dir))) token)))))
    (when-not handler
      (component.telegram.producer/produce! (parser/render-resource
                                              (format "%s/command_not_found.mustache"
                                                      (-> config :telegram :message-template-dir))) token))
    (commit-update-as-consumed! update-id (:bot telegram-consumer))))

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
                                        :telegram-consumer bot)
          pool       (at-at/mk-pool)]
      (at-at/interspaced 100 (partial consumer-job! consumers components) pool)
      (assoc component :telegram {:bot    bot
                                  :poller pool})))

  (stop [{:keys [telegram]}]
    (telegram-bot/close (:bot telegram))
    (at-at/stop-and-reset-pool! (:poller telegram))))       ;TODO: stop at-at pool here

(defn new-telegram
  [consumers]
  (map->Telegram {:consumers consumers}))
