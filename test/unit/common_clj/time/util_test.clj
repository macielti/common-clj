(ns common-clj.time.util-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.time.util :as time.util]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as s])
  (:import (java.time Instant)))

(s/deftest instant-now-test
  (testing "We should be able to instantiate a Instant representing now"
    (is (match? #(= (type %) Instant)
                (time.util/instant-now)))))
