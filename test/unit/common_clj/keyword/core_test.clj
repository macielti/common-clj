(ns common-clj.keyword.core-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.keyword.core :as keyword.core]
            [schema.test :as s]))

(s/deftest str->keyword-kebab-case-test
  (testing "that loweCamelCase strings can be converted to kebab case"
    (is (= :database-uri
           (keyword.core/str->keyword-kebab-case "databaseUri")))))

(s/deftest un-namespaced-test
  (testing "GIVEN a namespaced keword WHEN we unamespace it THEN It should be un-namespaced"
    (is (= :ok
           (keyword.core/un-namespaced :test/ok)))
    (is (= :deeper
           (keyword.core/un-namespaced :test.ok/deeper)))))
