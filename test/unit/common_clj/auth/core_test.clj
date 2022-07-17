(ns common-clj.auth.core-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.auth.core :as auth]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as s]))

(def token-content {:test :ok})
(def jwt-secret "very-top-secret")

(s/deftest ->token-test
  ;#TODO: Assert that we can decode the token
  (testing "that we can convert a map to a signed JWToken string"
    (is (match? string?
                (auth/->token token-content jwt-secret)))))
