(ns integration.telegram-producer-test
  (:require [clojure.test :refer :all]
            [schema.test :as s]
            [com.stuartsierra.component :as component]
            [matcher-combinators.test :refer [match?]]
            [common-clj.component.config :as component.config]
            [common-clj.component.telegram.producer :as component.telegram.producer]
            [common-clj.component.helper.core :as component.helper]))

(def ^:private system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test :json)
    :telegram-producer (component/using (component.telegram.producer/new-telegram-producer) [:config])))

(s/deftest telegram-producer-component-test
  (let [system            (component/start system-test)
        telegram-producer (component.helper/get-component-content :telegram-producer system)]

    (testing "that we can retrieve telegram bot token from component"
      (is (match? string?
                  telegram-producer)))
    (component/stop system)))
