(ns integration.new-relic-component-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.http-client :as component.http-client]
            [common-clj.component.new-relic :as component.new-relic]
            [schema.test :as s]
            [taoensso.timbre :as log]))

(def ^:private system-test
  (component/system-map
   :config (component.config/new-config "resources/config_test.edn" :test :edn)
   :http-client (component/using (component.http-client/new-http-client) [:config])
   :new-relic (component/using (component.new-relic/new-new-relic) [:config :http-client])))

(s/deftest new-relic-component-test
  (let [system (component/start system-test)
        http-client (component.helper/get-component-content :http-client system)]

    (log/warn :testing)

    (Thread/sleep 1000)

    (testing "that we can fetch the list of requests that was made diring an integration test execution"
      (is (= [{:method  :post
               :payload {:body    {:hostname  "bruno.nascimento"
                                   :level     "warn"
                                   :message   ":testing"
                                   :namespace "integration.new-relic-component-test"
                                   :service   "test-service-name"}
                         :headers {"Api-Key"      "random-api-key"
                                   "Content-Type" "application/json"}}
               :url     "https://log-api.newrelic.com/log/v1"}]
             (component.http-client/requests http-client))))

    (component/stop system)))