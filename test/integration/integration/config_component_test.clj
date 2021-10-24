(ns integration.config-component-test
  (:use [clojure pprint])
  (:require [clojure.test :refer :all]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.config :as component.config]
            [com.stuartsierra.component :as component]))

(def system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test)))

(def system-prod
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :prod)))

(deftest config-component-test
  (let [system            (component/start system-test)
        system-after-stop (component/stop-system system)]
    (testing "that we can start the config component"
      (is (true? (boolean (component.helper/get-component-content :config system)))))

    (testing "that we can stop the config component"
      (testing "that the stopped component exists"
        (is (true? (-> (get-in system-after-stop [:config])
                       (contains? :config)))))
      (testing "that the component was stopped"
        (is (false? (boolean (component.helper/get-component-content :config system-after-stop))))))))

(deftest config-component-content-test
  (testing "that we can get config content from config component"
    (let [system      (component/start system-test)
          {:keys [datomic-uri]} (component.helper/get-component-content :config system)]
      (is (= "datomic:mem://example-test"
             datomic-uri))
      (component/stop-system system))
    (let [system      (component/start system-prod)
          {:keys [datomic-uri]} (component.helper/get-component-content :config system)]
      (is (= "datomic:mem://example-prod"
             datomic-uri))
      (component/stop-system system))))
