(ns integration.config-component-test
  (:require [clojure.test :refer [is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.helper.core :as component.helper]
            [schema.test :as s]))

(def system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test :json)))

(def system-prod
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :prod :json)))

(s/deftest config-component-content-test
  (testing "that we can get config content from config component"
    (let [system (component/start system-test)
          {:keys [datomic-uri]} (component.helper/get-component-content :config system)]
      (is (= "datomic:mem://example-test"
             datomic-uri))
      (component/stop-system system))
    (let [system (component/start system-prod)
          {:keys [datomic-uri]} (component.helper/get-component-content :config system)]
      (is (= "datomic:mem://example-prod"
             datomic-uri))
      (component/stop-system system))))
