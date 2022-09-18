(ns common-clj.io.interceptors.auth-test
  (:require [clojure.test :refer :all]
            [schema.test :as s]
            [common-clj.io.interceptors.auth :as io.interceptors.auth]))

(def wire
  {:success true
   :score   0.8})

(def internal
  {:validation-result/score   0.8
   :validation-result/success true})

(s/deftest wire->google-recaptcha-v3-response-token-validation-result-test
  (testing "that we can internalize a Google reCAPTCHA v3 response token validation result wire http response"
    (is (= {:validation-result/score   0.8
            :validation-result/success true}
           (io.interceptors.auth/wire->google-recaptcha-v3-response-token-validation-result wire)))))

(deftest valid-recaptcha-v3-response-check?-test
  (testing "that we can interpretation the Google reCAPTCHA v3 response token validation result"
    (is (true? (io.interceptors.auth/valid-recaptcha-v3-response-check? internal)))
    (is (false? (io.interceptors.auth/valid-recaptcha-v3-response-check? (assoc internal :validation-result/success false))))
    (is (false? (io.interceptors.auth/valid-recaptcha-v3-response-check? (assoc internal :validation-result/score 0.6))))
    (is (false? (io.interceptors.auth/valid-recaptcha-v3-response-check? (assoc internal :validation-result/score 0.6
                                                                                         :validation-result/success false))))))
