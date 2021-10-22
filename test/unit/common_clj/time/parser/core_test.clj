(ns common-clj.time.parser.core-test
  (:require [clojure.test :refer :all]
            [common-clj.time.parser.core :as time.parser.core])
  (:import (java.time LocalDate)))

(def today (LocalDate/now))
(def yesterday (doto (LocalDate/now)
                 (.minusDays 1)))

(deftest str->local-date-test
  (testing "that we can parse strings that contain the word 'Hoje' to a LocalDate"
    (is (= today
           (time.parser.core/str->local-date "" "Hoje vai chover"))))
  (testing "that we can parse strings that contain the word 'Ontem' to a LocalDate"
    (is (= yesterday
           (time.parser.core/str->local-date "" "Ontem choveu"))))
  (testing "that we can parse date strings that contain a specific format to a LocalDate"
    (is (= (LocalDate/of 1998 12 26)
           (time.parser.core/str->local-date "dd-MM-yyyy" "26-12-1998")))))
