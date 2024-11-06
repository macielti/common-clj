(ns common-clj.time.core-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.time.core :as time]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as s])
  (:import (java.time LocalDate)
           (java.util Date)))

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
