(ns integration.config-component
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]))

(def system
  (component/system-map
    :config (component.config/new-config "resources/config_test.json")))

(deftest config-component-test
  (testing "that we can get config content from config component"
    (let [{{:keys [datomic-uri]} :config} (component/start system)]
      (is (= "datomic:mem://example"
             datomic-uri)))))¬
