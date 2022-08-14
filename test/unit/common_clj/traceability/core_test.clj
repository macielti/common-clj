(ns common-clj.traceability.core-test
  (:require [clojure.test :refer :all])
  (:require [common-clj.traceability.core :as common-traceability]))

(deftest current-correlation-id-from-request-context-test
  (is (= "DEFAULT.TEST"
         (common-traceability/current-correlation-id-from-request-context
           {:headers {"x-correlation-id" "default.test"}})))
  (is (= "DEFAULT"
         (common-traceability/current-correlation-id-from-request-context
           {:headers {}}))))
