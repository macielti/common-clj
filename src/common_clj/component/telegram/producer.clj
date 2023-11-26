(ns common-clj.component.telegram.producer
  (:require [com.stuartsierra.component :as component]))

(defrecord TelegramProducer [config]
  component/Lifecycle
  (start [component]
    (let [{{:keys [telegram] :as config-content} :config} config]
      (assoc component :telegram-producer {:token       (:token telegram)
                                           :current-env (:current-env config-content)})))

  (stop [component]
    component))

(defn new-telegram-producer
  []
  (->TelegramProducer {}))