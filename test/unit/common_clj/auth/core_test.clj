(ns common-clj.auth.core-test
  (:require [clojure.test :refer :all]
            [common-clj.auth.core :as auth]
            [matcher-combinators.test :refer [match?]]))

(def token-content {:test :ok})
(def jwt-secret "very-top-secret")

(deftest ->token-test
  ;#TODO: Assert that we can decode the token
  (testing "that we can convert a map to a signed JWToken string"
    (is (match? string?
               (auth/->token token-content jwt-secret)))))
