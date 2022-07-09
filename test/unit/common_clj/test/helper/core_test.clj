(ns common-clj.test.helper.core-test
  (:require [clojure.test :refer :all]
            [common-clj.test.helper.core :as test.helper]
            [matcher-combinators.test :refer [match?]]))

(deftest uuid-test
  (testing "that we can generate random Uuids"
    (is (match? uuid?
                (test.helper/uuid)))))
