(ns common-clj.io.interceptors.auth-test
  (:require [clojure.test :refer :all]
            [schema.test :as s]
            [common-clj.io.interceptors.auth :as io.interceptors.auth]))

(def wire
  {:success true
   :score   0.8})

(s/deftest wire->google-recaptcha-v3-response-token-validation-result-test
  (testing "that we can internalize a Google reCAPTCHA v3 response token validation result wire http response"
    (is (= {:validation-result/score   0.8
            :validation-result/success true}
           (io.interceptors.auth/wire->google-recaptcha-v3-response-token-validation-result wire)))))
