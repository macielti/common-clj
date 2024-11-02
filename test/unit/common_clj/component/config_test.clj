(ns common-clj.component.config-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.component.config :as component.config]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as s]))

(s/deftest read-config-file-test
  (testing "that we can read a edn config file"
    (is (match? {:prod {:bootstrap-server "http://localhost:9092"}}
                (component.config/read-config-file "test/resources/config_test.edn" :edn))))
  (testing "that we can read a jsonconfig file"
    (is (match? {:prod {:bootstrap-server "http://localhost:9092"}}
                (component.config/read-config-file "test/resources/config_test.json" :json)))))
