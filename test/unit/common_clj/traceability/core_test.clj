(ns common-clj.traceability.core-test
  (:require [clojure.test :refer :all]
            [mockfn.macros :as mfn]
            [common-clj.traceability.core :as common-traceability]
            [schema.test :as s]))

(s/deftest current-correlation-id-from-request-context-test
  (is (= "DEFAULT.TEST"
         (common-traceability/current-correlation-id-from-request-context
           {:headers {"x-correlation-id" "default.test"}})))
  (is (= "DEFAULT"
         (common-traceability/current-correlation-id-from-request-context
           {:headers {}}))))

(s/deftest correlation-id-appended-test
  (mfn/providing [(random-uuid) #uuid "7835733a-83e1-46d1-94e4-7b84fb601d64"]
                 (is (= "MOCK.TEST.7835733A-83E1-46D1-94E4-7B84FB601D64"
                        (common-traceability/correlation-id-appended "MOCK.TEST")))))
