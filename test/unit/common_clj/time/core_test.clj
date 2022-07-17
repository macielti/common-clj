(ns common-clj.time.core-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.time.core :as time]
            [mockfn.macros :as mfn]
            [schema.test :as s]))

(s/deftest now-datetime-test
  (testing "that we can generate a datetime now"
    (is (= #inst "2022-02-23T22:19:31.390-00:00"
           (mfn/providing [(time/now-datetime) #inst "2022-02-23T22:19:31.390-00:00"]
                          (time/now-datetime))))))
