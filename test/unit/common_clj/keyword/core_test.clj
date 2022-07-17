(ns common-clj.keyword.core-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.keyword.core :as keyword.core]
            [schema.test :as s]))

(s/deftest str->keyword-kebab-case-test
  (testing "that loweCamelCase strings can be converted to kebab case"
    (is (= :datomic-uri
           (keyword.core/str->keyword-kebab-case "datomicUri")))))
