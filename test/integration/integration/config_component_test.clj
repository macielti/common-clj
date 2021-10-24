(ns integration.config-component-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]))

(def system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test)))

(def system-prod
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :prod)))

(deftest config-component-test
  (testing "that we can get config content from config component"
    (let [{{:keys [datomic-uri]} :config :as system} (component/start system-test)]
      (is (= "datomic:mem://example-test"
             datomic-uri))
      (component/stop-system system))
    (let [{{:keys [datomic-uri]} :config :as system} (component/start system-prod)]
      (is (= "datomic:mem://example-prod"
             datomic-uri))
      (component/stop-system system))))
