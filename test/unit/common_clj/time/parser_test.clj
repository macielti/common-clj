(ns common-clj.time.parser-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.time.parser :as time.parser]
            [java-time.api :as jt]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as s])
  (:import (java.time Instant)
           (java.util Date)))

(s/deftest wire->instant-test
  (testing "Given a wire instant we should be able to convert it to internal Instant representation"
    (is (match? #(= (type %) Instant)
                (time.parser/wire->instant "2026-02-14T12:47:47.353626Z")))))

(s/deftest instant->wire-test
  (testing "Given a Instant we should be able to convert it to wire"
    (is (match? string?
                (time.parser/instant->wire (jt/instant))))))

(s/deftest instant->legacy-date-test
  (testing "Given a Instant we should be able to convert it to legacy Date"
    (is (match? #(= (type %) Date)
                (time.parser/instant->legacy-date (jt/instant))))))

(s/deftest legacy-date->instant-test
  (testing "Given a legacy Date we should be able to convert it to Instant"
    (is (match? #(= (type %) Instant)
                (time.parser/legacy-date->instant (Date.))))))
