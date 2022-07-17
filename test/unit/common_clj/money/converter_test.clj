(ns common-clj.money.converter-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.money.converter :as money.converter]
            [schema.test :as s]))

(s/deftest ->cents-test
  (testing "should be able to convert money value to cents"
    (is (= 200N
           (money.converter/->cents 2N)))
    (is (= 200N
           (money.converter/->cents 2M)))
    (is (= 200N
           (money.converter/->cents 2)))
    (is (= 200N
           (money.converter/->cents 2.0)))
    (is (= 215N
           (money.converter/->cents 2.15)))))
