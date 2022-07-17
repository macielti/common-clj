(ns common-clj.time.parser.core-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.time.parser.core :as time.parser.core]
            [schema.test :as s])
  (:import (java.time LocalDate)))

(def date (LocalDate/of 1998 12 26))
(def today (LocalDate/now))
(def yesterday (doto (LocalDate/now)
                 (.minusDays 1)))

(s/deftest str->local-date-test
  (testing "that we can parse strings that contain the word 'Hoje' to a LocalDate"
    (is (= today
           (time.parser.core/str->local-date "" "Hoje vai chover"))))
  (testing "that we can parse strings that contain the word 'Ontem' to a LocalDate"
    (is (= yesterday
           (time.parser.core/str->local-date "" "Ontem choveu"))))
  (testing "that we can parse date strings that contain a specific format to a LocalDate"
    (is (= date
           (time.parser.core/str->local-date "dd-MM-yyyy" "26-12-1998")))))

(s/deftest local-date->str-test
  (testing "that we can parse LocalDate to formatted date string"
    (is (= "26-12-1998"
           (time.parser.core/local-date->str "dd-MM-yyyy" date)))
    (is (= "1998"
           (time.parser.core/local-date->str "yyyy" date)))
    (is (= "26/12/1998"
           (time.parser.core/local-date->str "dd/MM/yyyy" date)))))
