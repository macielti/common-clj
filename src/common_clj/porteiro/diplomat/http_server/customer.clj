(ns common-clj.porteiro.diplomat.http-server.customer
  (:require [common-clj.porteiro.adapters.customer :as adapters.customer]
            [common-clj.porteiro.controllers.customer :as controllers.customer]
            [datomic.api :as d]
            [schema.core :as s])
  (:import (java.util UUID)))

(s/defn create-customer!
  [{{:keys [customer]} :json-params
    {:keys [datomic]}  :components}]
  {:status 201
   :body   {:customer (-> (adapters.customer/wire->internal customer)
                          (controllers.customer/create-customer! datomic)
                          adapters.customer/internal->wire)}})

(s/defn authenticate-customer!
  [{{:keys [customer]}       :json-params
    {:keys [datomic config]} :components}]
  {:status 200
   :body   (-> (adapters.customer/wire->internal-customer-authentication customer)
               (controllers.customer/authenticate-customer! config (d/db datomic))
               adapters.customer/customer-token->wire)})

(s/defn add-role!
  [{{wire-customer-id :customer-id
     wire-role        :role} :query-params
    {:keys [datomic]}        :components}]
  {:status 200
   :body   {:customer (-> (UUID/fromString wire-customer-id)
                          (controllers.customer/add-role! (adapters.customer/wire->internal-role wire-role) datomic)
                          adapters.customer/internal->wire)}})
