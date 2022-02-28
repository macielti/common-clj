(ns common-clj.error.core-test
  (:require [clojure.test :refer :all]
            [common-clj.error.core :as error])
  (:import (clojure.lang ExceptionInfo)))

(deftest http-friendly-exception-test
  (testing "that we can throw http friendly exception responses"
    (try (error/http-friendly-exception 422 "invalid" "invalid operation" "error while trying to do something")
         (catch ExceptionInfo e
           (is (= {:status  422
                   :error   "invalid"
                   :message "invalid operation"
                   :detail  "error while trying to do something"}
                  (ex-data e)))))))
