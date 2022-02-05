(ns common-clj.component.telegram.producer
  (:require [schema.core :as s]
            [com.stuartsierra.component :as component]
            [telegrambot-lib.core :as telegram-bot]
            [morse.api :as morse-api]))

(s/defn produce!
  [chat-id :- s/Int
   message :- s/Str
   telegram-producer]
  (morse-api/send-text telegram-producer chat-id message))

(defrecord TelegramProducer [config]
  component/Lifecycle
  (start [component]
    (let [{{:keys [telegram]} :config} config]
      (assoc component :telegram-producer (:token telegram))))

  (stop [{:keys [telegram-producer]}]
    (telegram-bot/close telegram-producer)))

(defn new-telegram-producer []
  (map->TelegramProducer {}))
