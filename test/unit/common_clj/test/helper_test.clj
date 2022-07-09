(ns common-clj.test.helper-test
  (:require [clojure.test :refer :all]
            [common-clj.test.helper :as test.helper]
            [matcher-combinators.test :refer [match?]]))

(deftest uuid-test
  (testing "that we can generate random Uuids"
    (is (match? uuid?
           (test.helper/uuid)))))
