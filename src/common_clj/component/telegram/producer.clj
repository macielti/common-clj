(ns common-clj.component.telegram.producer
  (:require [com.stuartsierra.component :as component]
            [morse.api :as morse-api]
            [schema.core :as s]))

(s/defn produce!
  [chat-id :- s/Int
   message :- s/Str
   token]
  (morse-api/send-text token chat-id message))

(defrecord TelegramProducer [config handler]
  component/Lifecycle
  (start [component]
    (let [{{:keys [telegram]} :config} config]
      (assoc component :telegram-producer (:token telegram))))

  (stop [component]
    (assoc component :telegram-producer nil)))

(defn new-telegram-producer []
  (map->TelegramProducer {}))
