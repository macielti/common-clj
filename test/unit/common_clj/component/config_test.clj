(ns common-clj.component.config-test
  (:require [clojure.test :refer :all]
            [schema.test :as s]
            [matcher-combinators.test :refer [match?]]
            [common-clj.component.config :as component.config]))


(s/deftest read-config-file-test
  (testing "that we can read a edn config file"
    (is (match? {:prod {:bootstrap-server "http://localhost:9092"}}
                (component.config/read-config-file "resources/config_test.edn" :edn))))
  (testing "that we can read a jsonconfig file"
    (is (match? {:prod {:bootstrap-server "http://localhost:9092"}}
                (component.config/read-config-file "resources/config_test.json" :json)))))
