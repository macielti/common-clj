(ns common-clj.test.helper.time-test
  (:require [clojure.test :refer :all]
            [common-clj.test.helper.time :as test.helper.time]))

(deftest valid-iso-8601?-test
  (testing "that we can validate iso-8601 date string"
    (is (not (test.helper.time/valid-iso-8601? "invalid date")))
    (is (test.helper.time/valid-iso-8601? "2022-07-22T22:59:17.598Z"))))
