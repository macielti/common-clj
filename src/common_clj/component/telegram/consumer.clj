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
            [telegrambot-lib.core :as telegram-bot]
            [taoensso.timbre :as log])
  (:import (clojure.lang ExceptionInfo)))

(s/defn ^:private commit-update-as-consumed!
  [offset :- s/Int
   telegram-bot]
  (telegram-bot/get-updates telegram-bot {:offset (+ offset 1)}))

(s/defn ^:private mock-commit-update-as-consumed!
  [update
   {:keys [consumed-updates]}]
  (swap! consumed-updates conj update))

(s/defn interceptors-by-consumer
  [consumer
   {:keys [interceptors]}]
  (let [interceptor-groups (group-by :name interceptors)]
    (map #(-> (get interceptor-groups %) first) (:consumer/interceptors consumer))))

(defmulti ^:private consume-update!
          (fn [_update
               _consumers
               {:keys [config] :as _components}]
            (:current-env config)))

(s/defmethod ^:private consume-update! :prod
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

(s/defmethod ^:private consume-update! :test
             [update
              consumers :- component.telegram.models.consumer/Consumers
              {:keys [telegram-consumer] :as components}]
             (let [{:consumer/keys [handler] :as consumer} (telegram.adapters.message/update->consumer update consumers)
                   context {:update     update
                            :components components}]
               (when (and handler update)
                 (try
                   (chain/execute context
                                  (concat (interceptors-by-consumer consumer consumers)
                                          [(interceptor/interceptor {:name  :handler-interceptor
                                                                     :enter handler})]))))
               (mock-commit-update-as-consumed! update telegram-consumer)))

(defmulti ^:private consumer-job!
          (fn [_consumers
               {:keys [config] :as _components}]
            (:current-env config)))

(s/defmethod ^:private consumer-job! :prod
             [consumers
              {:keys [telegram-consumer] :as components}]
             (when-let [updates (-> (telegram-bot/get-updates telegram-consumer) :result)]
               (doseq [update updates]
                 (consume-update! update consumers components))))

(s/defn ^:private not-consumed-incoming-updates
  [incoming-updates
   consumed-updates]
  (clojure.set/difference incoming-updates consumed-updates))

(s/defmethod ^:private consumer-job! :test
             [consumers
              {:keys [telegram-consumer] :as components}]
             (when-let [updates (not-consumed-incoming-updates @(:incoming-updates telegram-consumer)
                                                               @(:consumed-updates telegram-consumer))]
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
      (assoc component :telegram-consumer {:bot         bot
                                           :poller      pool
                                           :current-env (:current-env config-content)})))

  (stop [{:keys [telegram-consumer]}]
    (telegram-bot/close (:bot telegram-consumer))
    (at-at/stop-and-reset-pool! (:poller telegram-consumer))))

(defn new-telegram-consumer
  [consumers]
  (map->TelegramConsumer {:consumers consumers}))


(defrecord MockTelegramConsumer [config datomic consumers]
  component/Lifecycle
  (start [component]
    (let [pool (at-at/mk-pool)
          telegram-consumer-component {:incoming-updates (atom [])
                                       :consumed-updates (atom [])
                                       :current-env      (-> config :config :current-env)
                                       :pool             pool}
          components (medley/assoc-some {:telegram-consumer telegram-consumer-component}
                                        :datomic (:datomic datomic)
                                        :config (:config config))]

      (at-at/interspaced 100 (fn []
                               (try (consumer-job! consumers components)
                                    (catch ExceptionInfo ex
                                      (log/error ex)))) pool)

      (assoc component :telegram-consumer telegram-consumer-component)))

  (stop [{:keys [telegram-consumer]}]
    (at-at/stop-and-reset-pool! (:pool telegram-consumer))))

(defn new-mock-telegram-consumer
  [consumers]
  (->MockTelegramConsumer {} {} consumers))

(s/defn insert-incoming-update!
  [update
   telegram-consumer]
  (swap! (:incoming-updates telegram-consumer) conj update))
