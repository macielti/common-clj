(ns integration.telegram-producer-component-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.telegram.producer :as component.telegram.producer]
            [schema.test :as s]))

(def system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test :json)
    :telegram-producer (component/using (component.telegram.producer/new-telegram-producer) [:config])))

(s/deftest telegram-consumer-component-test
  (let [system (component/start system-test)
        {:keys [produced] :as producer} (component.helper/get-component-content :telegram-producer system)]

    (testing "That we can consume a telegram bot command"
      (component.telegram.producer/send-text! {:chat-id "123456789"
                                               :text    "Hello World"
                                               :options {:test-option :text}} producer)

      (is (= [{:chat-id "123456789"
               :options {:test-option :text}
               :text    "Hello World"}]
             @produced)))

    (component/stop system)))
