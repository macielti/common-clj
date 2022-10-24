(ns common-clj.component.telegram.mock-consumer
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

(s/defn ^:private consumer-job!
  [consumers
   {:keys [telegram-consumer] :as components}]
  (when-let [updates (-> (telegram-bot/get-updates telegram-consumer) :result)]
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