(ns integration.telegram-consumer-component-test
  (:require [clojure.test :refer [is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.telegram.consumer :as component.telegram.consumer]
            [schema.test :as s]
            [common-clj.component.helper.core :as component.helper]))

(def test-state (atom nil))

(defn test-consumer!
  [{:keys [update]}]
  (reset! test-state update))

(def consumers
  {:message {:test {:consumer/handler test-consumer!}}})

(def system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test :json)
    :telegram-consumer (component/using (component.telegram.consumer/new-mock-telegram-consumer consumers) [:config])))

(s/deftest telegram-consumer-component-test
  (let [system (component/start system-test)
        telegram-consumer (component.helper/get-component-content :telegram-consumer system)]

    (testing "That we can consume a telegram bot command"
      (component.telegram.consumer/insert-incoming-update! {:message {:text "/test param1 param2"}} telegram-consumer)
      (Thread/sleep 5000)
      (is (= {:message {:text "/test param1 param2"}}
             @test-state))
      (reset! test-state nil))

    (testing "Not recognized telegram bot command is not consumed"
      (component.telegram.consumer/insert-incoming-update! {:message {:text "/wring-command param1 param2"}} telegram-consumer)
      (Thread/sleep 5000)
      (is (= nil
             @test-state)))

    (component/stop system)))