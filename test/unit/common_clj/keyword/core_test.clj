(ns common-clj.keyword.core-test
  (:require [schema.test :as s]
            [clojure.test :refer :all]
            [common-clj.keyword.core :as keyword.core]))

(use-fixtures :once s/validate-schemas)

(s/deftest str->keyword-kebab-case-test
  (testing "that loweCamelCase strings can be converted to kebab case"
    (is (= :datomic-uri
           (keyword.core/str->keyword-kebab-case "datomicUri")))))
