(ns common-clj.time.core-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.time.core :as time]
            [mockfn.macros :as mfn]
            [schema.test :as s]
            [matcher-combinators.test :refer [match?]])
  (:import (java.time LocalDate)
           (java.util Date)))

(s/deftest now-datetime-test
  (testing "that we can generate a datetime now"
    (is (= #inst "2022-02-23T22:19:31.390-00:00"
           (mfn/providing [(time/now-datetime) #inst "2022-02-23T22:19:31.390-00:00"]
                          (time/now-datetime))))))

(s/deftest now-test
  (testing "now local datetime"
    (is (match? time/local-datetime?
                (time/now)))))

(s/deftest date->local-datetime-test
  (testing "that we can convert a Date to LocalDateTime"
    (is (match? time/local-datetime?
                (time/date->local-datetime (Date.))))))

(s/deftest local-datetime->date-test
  (testing "that we can convert LocalDateTime to Date"
    (is (match? inst?
                (time/local-datetime->date (time/now))))))

(s/deftest date-to-local-date-test
  (testing "that we can convert a Date to LocalDate"
    (is (match? #(= (type %) LocalDate)
                (time/date->local-date (Date.))))))
