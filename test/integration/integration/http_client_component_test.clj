(ns integration.http-client-component-test
  (:require [clojure.test :refer [is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.http-client :as component.http-client]
            [schema.test :as s]))

(def ^:private system-test
  (component/system-map
   :config (component.config/new-config "test/resources/config_test.edn" :test :edn)
   :http-client (component/using (component.http-client/new-http-client) [:config])))

(s/deftest http-client-component-test
  (let [system (component/start system-test)
        http-client (component.helper/get-component-content :http-client system)]
    (component.http-client/request! {:url    "https://google.com.br"
                                     :method :get}
                                    http-client)
    (testing "that we can fetch the list of requests that was made diring an integration test execution"
      (is (= [{:method :get
               :url    "https://google.com.br"}]
             (component.http-client/requests http-client))))
    (component/stop-system system)))
