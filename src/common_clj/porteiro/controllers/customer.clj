(ns common-clj.porteiro.controllers.customer
  (:require [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]
            [common-clj.error.core :as common-error]
            [common-clj.porteiro.adapters.customer :as adapters.customer]
            [common-clj.porteiro.db.datomic.customer :as database.customer]
            [common-clj.porteiro.models.customer :as models.customer]
            [java-time.api :as jt]
            [schema.core :as s]))

(s/defn create-customer! :- models.customer/Customer
  [customer :- models.customer/Customer
   datomic]
  (database.customer/insert! customer datomic))

(s/defn ->token :- s/Str
  [map :- {s/Keyword s/Any}
   jwt-secret :- s/Str]
  (jwt/sign map jwt-secret {:exp (-> (jt/local-date-time (jt/zone-id "UTC"))
                                     (jt/plus (jt/days 1))
                                     (jt/sql-timestamp))}))

(s/defn authenticate-customer! :- s/Str
  [{:customer/keys [username password]} :- models.customer/CustomerAuthentication
   {:keys [jwt-secret]}
   database]
  (let [{:customer/keys [hashed-password] :as customer} (database.customer/by-username username database)]
    (if (and customer (:valid (hashers/verify password hashed-password)))
      (-> {:customer (adapters.customer/internal->wire customer)}
          (->token jwt-secret))
      (common-error/http-friendly-exception 403
                                            "invalid-credentials"
                                            "Wrong username or/and password"
                                            "Customer is trying to login using invalid credentials"))))
