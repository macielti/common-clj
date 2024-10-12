(ns integration.porteiro.fixtures.customer
  (:require [clojure.test :refer :all]
            [common-clj.porteiro.wire.in.customer :as porteiro.wire.in.customer]
            [common-clj.test.helper.schema :as test.helper.schema]))

(def customer-username "ednaldo-pereira")
(def customer-password "password")

(def customer-creation-document
  (test.helper.schema/generate porteiro.wire.in.customer/CustomerCreationDocument
                               {:customer {:username customer-username
                                           :password customer-password}}))

(def customer-authentication-document
  (test.helper.schema/generate porteiro.wire.in.customer/CustomerAuthenticationDocument
                               {:customer {:username customer-username
                                           :password customer-password}}))

(def admin-customer-authentication-document
  (test.helper.schema/generate porteiro.wire.in.customer/CustomerAuthenticationDocument
                               {:customer {:username "admin"
                                           :password "password"}}))
