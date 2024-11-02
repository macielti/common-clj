(ns integration.new-relic-component-test
  (:require [clojure.test :refer [is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.http-client :as component.http-client]
            [common-clj.component.new-relic :as component.new-relic]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as s]
            [taoensso.timbre :as log]))

(def ^:private system-test
  (component/system-map
   :config (component.config/new-config "test/resources/config_test.edn" :test :edn)
   :http-client (component/using (component.http-client/new-http-client) [:config])
   :new-relic (component/using (component.new-relic/new-new-relic) [:config :http-client])))

(s/deftest new-relic-component-test
  (let [system (component/start system-test)
        http-client (component.helper/get-component-content :http-client system)]

    (log/warn ::testing {:service :random-service-name})

    (Thread/sleep 1000)

    (testing "that we can fetch the requests made by the http-client sent to new relic"
      (is (match? [{:method  :post
                    :payload {:body    {:hostname  string?
                                        :level     "warn"
                                        :cid       string?
                                        :log       ":integration.new-relic-component-test/testing {:service :random-service-name}"
                                        :namespace "integration.new-relic-component-test"
                                        :service   "test-service-name"}
                              :headers {"Api-Key"      "random-api-key"
                                        "Content-Type" "application/json"}}
                    :url     "https://log-api.newrelic.com/log/v1"}]
                  (component.http-client/requests http-client))))

    (component/stop system)))
