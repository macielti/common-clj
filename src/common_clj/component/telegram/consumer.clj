(ns common-clj.component.telegram.consumer
  (:require [clostache.parser :as parser]
            [com.stuartsierra.component :as component]
            [common-clj.component.telegram.adapters.update :as telegram.adapters.message]
            [common-clj.component.telegram.models.consumer :as component.telegram.models.consumer]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.interceptor.chain :as chain]
            [medley.core :as medley]
            [morse.api :as morse-api]
            [overtone.at-at :as at-at]
            [schema.core :as s]
            [taoensso.timbre :as log]
            [telegrambot-lib.core :as telegram-bot])
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
    (map #(-> (get interceptor-groups %) first) (:interceptors consumer))))

(defmulti consume-update!
  (fn [_update
       _consumers
       {:keys [config] :as _components}]
    (:current-env config)))

(s/defmethod consume-update! :prod
  [update
   consumers :- component.telegram.models.consumer/Consumers
   {:keys [telegram-consumer config] :as components}]
  (let [{:update/keys [chat-id id type] :as update'} (telegram.adapters.message/wire->internal update)
        {:keys [handler interceptors error-handler] :as consumer} (telegram.adapters.message/update->consumer update' consumers)
        token (-> config :telegram :token)
        context {:update     update'
                 :components components}]
    (when (and handler update' id)
      (try
        (chain/execute context
                       (concat interceptors
                               [(interceptor/interceptor {:name  :handler-interceptor
                                                          :enter handler})]))
        (catch Exception e
          (if error-handler
            (error-handler e components)
            (do (log/error e)
                (morse-api/send-text token chat-id (parser/render-resource
                                                    (format "%s/error_processing_message_command.mustache"
                                                            (-> config :telegram :message-template-dir)))))))))
    (when (and (not handler) (not= type :others))
      (morse-api/send-text token chat-id (parser/render-resource
                                          (format "%s/command_not_found.mustache"
                                                  (-> config :telegram :message-template-dir)))))
    (commit-update-as-consumed! id telegram-consumer)))

(s/defmethod consume-update! :test
  [update
   consumers :- component.telegram.models.consumer/Consumers
   {:keys [telegram-consumer] :as components}]
  (let [update' (telegram.adapters.message/wire->internal update)
        {:keys [handler interceptors] :as consumer} (telegram.adapters.message/update->consumer update' consumers)
        context {:update     update'
                 :components components}]
    (when (and handler update)
      (try
        (chain/execute context
                       (concat interceptors
                               [(interceptor/interceptor {:name  :handler-interceptor
                                                          :enter handler})]))))
    (when (:consumed-updates telegram-consumer)
      (mock-commit-update-as-consumed! update telegram-consumer))))

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

(defrecord TelegramConsumer [config http-client prometheus datomic datalevin jobs telegram-producer consumers]
  component/Lifecycle
  (start [component]
    (let [{{:keys [telegram] :as config-content} :config} config
          bot (telegram-bot/create (:token telegram))
          components (medley/assoc-some {}
                                        :http-client (:http-client http-client)
                                        :datomic (:datomic datomic)
                                        :datalevin (:datalevin datalevin)
                                        :config config-content
                                        :telegram-consumer bot
                                        :jobs (:jobs jobs)
                                        :prometheus (:prometheus prometheus)
                                        :telegram-producer (:telegram-producer telegram-producer))
          pool (at-at/mk-pool)]
      (at-at/interspaced 100 (fn []
                               (try (consumer-job! consumers components)
                                    (catch ExceptionInfo ex
                                      (log/error ex)))) pool)
      (assoc component :telegram-consumer {:bot         bot
                                           :poller      pool
                                           :current-env (:current-env config-content)})))

  (stop [{:keys [telegram-consumer]}]
    (telegram-bot/close (:bot telegram-consumer))
    (at-at/stop-and-reset-pool! (:poller telegram-consumer))))

(defn new-telegram-consumer
  [consumers]
  (->TelegramConsumer {} {} {} {} {} {} {} consumers))

(defrecord MockTelegramConsumer [config http-client datomic datalevin telegram-producer consumers]
  component/Lifecycle
  (start [component]
    (let [pool (at-at/mk-pool)
          telegram-consumer-component {:incoming-updates (atom [])
                                       :consumed-updates (atom [])
                                       :current-env      (-> config :config :current-env)
                                       :pool             pool}
          components (medley/assoc-some {:telegram-consumer telegram-consumer-component}
                                        :http-client (:http-client http-client)
                                        :datomic (:datomic datomic)
                                        :datalevin (:datalevin datalevin)
                                        :config (:config config)
                                        :telegram-producer (:telegram-producer telegram-producer))]

      (at-at/interspaced 100 (fn []
                               (try (consumer-job! consumers components)
                                    (catch ExceptionInfo ex
                                      (log/error ex)))) pool)

      (assoc component :telegram-consumer telegram-consumer-component)))

  (stop [{:keys [telegram-consumer]}]
    (at-at/stop-and-reset-pool! (:pool telegram-consumer))))

(defn new-mock-telegram-consumer
  [consumers]
  (->MockTelegramConsumer {} {} {} {} {} consumers))

(s/defn insert-incoming-update!
  [update
   telegram-consumer]
  (swap! (:incoming-updates telegram-consumer) conj update))
