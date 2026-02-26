(ns common-clj.schema.extensions-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.schema.extensions :as extensions]
            [schema.core :as s]
            [schema.test])
  (:import (clojure.lang ExceptionInfo)))

(schema.test/deftest positive-int-test
  (testing "Should accept positive integers"
    (is (= 1 (s/validate extensions/PositiveInt 1)))
    (is (= 5 (s/validate extensions/PositiveInt 5)))
    (is (= 100 (s/validate extensions/PositiveInt 100)))
    (is (= Integer/MAX_VALUE (s/validate extensions/PositiveInt Integer/MAX_VALUE))))

  (testing "Should reject zero"
    (is (thrown? ExceptionInfo (s/validate extensions/PositiveInt 0))))

  (testing "Should reject negative integers"
    (is (thrown? ExceptionInfo (s/validate extensions/PositiveInt -1)))
    (is (thrown? ExceptionInfo (s/validate extensions/PositiveInt -100)))
    (is (thrown? ExceptionInfo (s/validate extensions/PositiveInt Integer/MIN_VALUE))))

  (testing "Should reject non-integer types"
    (is (thrown? ExceptionInfo (s/validate extensions/PositiveInt "5")))
    (is (thrown? ExceptionInfo (s/validate extensions/PositiveInt 5.5)))
    (is (thrown? ExceptionInfo (s/validate extensions/PositiveInt :five)))
    (is (thrown? ExceptionInfo (s/validate extensions/PositiveInt nil)))))

(schema.test/deftest non-negative-int-test
  (testing "Should accept zero and positive integers"
    (is (= 0 (s/validate extensions/NonNegativeInt 0)))
    (is (= 1 (s/validate extensions/NonNegativeInt 1)))
    (is (= 5 (s/validate extensions/NonNegativeInt 5)))
    (is (= 100 (s/validate extensions/NonNegativeInt 100)))
    (is (= Integer/MAX_VALUE (s/validate extensions/NonNegativeInt Integer/MAX_VALUE))))

  (testing "Should reject negative integers"
    (is (thrown? ExceptionInfo (s/validate extensions/NonNegativeInt -1)))
    (is (thrown? ExceptionInfo (s/validate extensions/NonNegativeInt -100)))
    (is (thrown? ExceptionInfo (s/validate extensions/NonNegativeInt Integer/MIN_VALUE))))

  (testing "Should reject non-integer types"
    (is (thrown? ExceptionInfo (s/validate extensions/NonNegativeInt "0")))
    (is (thrown? ExceptionInfo (s/validate extensions/NonNegativeInt 5.5)))
    (is (thrown? ExceptionInfo (s/validate extensions/NonNegativeInt :zero)))
    (is (thrown? ExceptionInfo (s/validate extensions/NonNegativeInt nil)))))
