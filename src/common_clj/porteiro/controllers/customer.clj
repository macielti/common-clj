(ns common-clj.porteiro.controllers.customer
  (:require [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]
            [common-clj.error.core :as common-error]
            [common-clj.porteiro.adapters.customer :as adapters.customer]
            [common-clj.porteiro.db.datomic.customer :as datomic.customer]
            [common-clj.porteiro.db.postgresql.customer :as postgresql.customer]
            [common-clj.porteiro.models.customer :as models.customer]
            [datomic.api :as d]
            [java-time.api :as jt]
            [pg.pool :as pool]
            [schema.core :as s]))

(s/defn create-customer! :- models.customer/Customer
  [customer :- models.customer/Customer
   datomic
   postgresql]
  (if datomic
    (datomic.customer/insert! customer datomic)
    (pool/with-connection [conn postgresql]
      (postgresql.customer/insert! customer conn))))

(s/defn ->token :- s/Str
  [map :- {s/Keyword s/Any}
   jwt-secret :- s/Str]
  (jwt/sign map jwt-secret {:exp (-> (jt/local-date-time (jt/zone-id "UTC"))
                                     (jt/plus (jt/days 1))
                                     (jt/sql-timestamp))}))

(s/defn authenticate-customer! :- s/Str
  [{:keys [username password]} :- models.customer/CustomerAuthentication
   {:keys [jwt-secret]}
   datomic
   postgresql]
  (let [{:customer/keys [hashed-password] :as customer} (if datomic
                                                          (datomic.customer/by-username username (d/db datomic))
                                                          (pool/with-connection [conn postgresql]
                                                            (postgresql.customer/by-username username conn)))]
    (if (and customer (:valid (hashers/verify password hashed-password)))
      (-> {:customer (adapters.customer/internal->wire customer)}
          (->token jwt-secret))
      (common-error/http-friendly-exception 403
                                            "invalid-credentials"
                                            "Wrong username or/and password"
                                            "Customer is trying to login using invalid credentials"))))

(defmulti add-role!
  (fn [_customer-id _role datomic postgresql]
    (cond datomic :datomic
          postgresql :postgresql)))

(s/defmethod add-role! :datomic
  [customer-id :- s/Uuid
   role :- s/Keyword
   datomic
   _]
  (if (datomic.customer/lookup customer-id (d/db datomic))
    (do (datomic.customer/add-role! customer-id role datomic) ;;TODO: Check how to make this transaction already return the updated entity
        (datomic.customer/lookup customer-id (d/db datomic)))
    (throw (ex-info "Customer not found"
                    {:status 404
                     :cause  "Customer not found"}))))

(s/defmethod add-role! :postgresql
  [customer-id :- s/Uuid
   role :- s/Keyword
   _
   postgresql]
  (pool/with-connection [conn postgresql]
    (if (postgresql.customer/lookup customer-id conn)
      (postgresql.customer/add-role! customer-id role conn)
      (throw (ex-info "Customer not found"
                      {:status 404
                       :cause  "Customer not found"})))))
