(ns common-clj.component.telegram.mock-consumer
  (:require [com.stuartsierra.component :as component]
            [common-clj.component.telegram.adapters.update :as telegram.adapters.message]
            [common-clj.component.telegram.models.consumer :as component.telegram.models.consumer]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.interceptor.chain :as chain]
            [medley.core :as medley]
            [overtone.at-at :as at-at]
            [schema.core :as s]
            [common-clj.component.telegram.consumer :as component.telegram.consumer]))

(s/defn commit-update-as-consumed!
  [update
   {:keys [consumed-updates]}]
  (swap! consumed-updates conj update))

(s/defn consume-update!
  [update
   consumers :- component.telegram.models.consumer/Consumers
   {:keys [telegram-consumer] :as components}]
  (let [{:consumer/keys [handler] :as consumer} (telegram.adapters.message/update->consumer update consumers)
        context {:update     update
                 :components components}]
    (when (and handler update)
      (try
        (chain/execute context
                       (concat (component.telegram.consumer/interceptors-by-consumer consumer consumers)
                               [(interceptor/interceptor {:name  :handler-interceptor
                                                          :enter handler})]))))
    (commit-update-as-consumed! update telegram-consumer)))

(s/defn ^:private not-consumed-incoming-updates
  [incoming-updates
   consumed-updates]
  (clojure.set/difference incoming-updates consumed-updates))

(s/defn ^:private consumer-job!
  [consumers
   {:keys [telegram-consumer] :as components}]
  (when-let [updates (not-consumed-incoming-updates @(:incoming-updates telegram-consumer)
                                                    @(:consumed-updates telegram-consumer))]
    (doseq [update updates]
      (consume-update! update consumers components))))

(defrecord MockTelegramConsumer [config datomic consumers]
  component/Lifecycle
  (start [component]
    (let [pool (at-at/mk-pool)
          telegram-consumer-component {:incoming-updates (atom [])
                                       :consumed-updates (atom [])
                                       :pool             pool}
          components (medley/assoc-some {:telegram-consumer telegram-consumer-component}
                                        :datomic (:datomic datomic)
                                        :config (:config config))]

      (at-at/interspaced 100 (partial consumer-job! consumers components) pool)

      (assoc component :telegram-consumer telegram-consumer-component)))

  (stop [{:keys [telegram-consumer]}]
    (at-at/stop-and-reset-pool! (:pool telegram-consumer))))