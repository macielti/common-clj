(ns common-clj.porteiro.interceptors.customer
  (:require [common-clj.error.core :as common-error]
            [common-clj.porteiro.db.datomic.customer :as datomic.customer]
            [common-clj.porteiro.db.postgresql.customer :as postgresql.customer]
            [datomic.api :as d]
            [pg.pool :as pool]))

(def username-already-in-use-interceptor
  {:name  ::username-already-in-use-interceptor
   :enter (fn [{{json-params                  :json-params
                 {:keys [datomic postgresql]} :components} :request :as context}]
            (let [username (get-in json-params [:customer :username] "")
                  customer (if datomic
                             (datomic.customer/by-username username (d/db datomic))
                             (pool/with-connection [database-conn postgresql]
                               (postgresql.customer/by-username username database-conn)))]
              (when-not (empty? customer)
                (common-error/http-friendly-exception 409
                                                      "not-unique"
                                                      "Username already in use"
                                                      "username already in use by another customer")))
            context)})
