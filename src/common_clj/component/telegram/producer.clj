(ns common-clj.component.telegram.producer
  (:require [com.stuartsierra.component :as component]
            [medley.core :as medley]
            [morse.api :as morse-api]
            [schema.core :as s]))

(s/defschema SendTextPayload
  {:chat-id                  s/Str
   :text                     s/Str
   (s/optional-key :options) (s/pred map?)})

(defmulti send-text!
  (fn [_ {:keys [current-env]}]
    current-env))

(s/defmethod send-text! :prod
  [{:keys [chat-id text options]
    :or   {options {}}} :- SendTextPayload
   {:keys [token]}]
  (morse-api/send-text token chat-id options text))

(s/defmethod send-text! :test
  [payload :- SendTextPayload
   {:keys [produced]}]
  (swap! produced conj payload))

(defrecord TelegramProducer [config]
  component/Lifecycle
  (start [component]
    (let [{{:keys [telegram] :as config-content} :config} config]
      (assoc component :telegram-producer (medley/assoc-some {:token       (:token telegram)
                                                              :current-env (:current-env config-content)}
                                                             :produced (when (= (:current-env config-content) :test)
                                                                         (atom []))))))

  (stop [component]
    component))

(defn new-telegram-producer
  []
  (->TelegramProducer {}))