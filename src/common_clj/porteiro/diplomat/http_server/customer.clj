(ns common-clj.porteiro.diplomat.http-server.customer
  (:require [common-clj.porteiro.adapters.customer :as adapters.customer]
            [common-clj.porteiro.controllers.customer :as controllers.customer]
            [schema.core :as s])
  (:import (java.util UUID)))

(s/defn create-customer!
  [{{:keys [customer]}   :json-params
    {:keys [postgresql]} :components}]
  {:status 201
   :body   {:customer (-> (adapters.customer/wire->internal customer)
                          (controllers.customer/create-customer! postgresql)
                          adapters.customer/internal->wire)}})

(s/defn authenticate-customer!
  [{{:keys [customer]}          :json-params
    {:keys [postgresql config]} :components}]
  {:status 200
   :body   (-> (adapters.customer/wire->internal-customer-authentication customer)
               (controllers.customer/authenticate-customer! config postgresql)
               adapters.customer/customer-token->wire)})

(s/defn add-role!
  [{{wire-customer-id :customer-id
     wire-role        :role} :query-params
    {:keys [postgresql]}     :components}]
  {:status 200
   :body   (-> (UUID/fromString wire-customer-id)
               (controllers.customer/add-role! (adapters.customer/wire->internal-role wire-role) postgresql)
               adapters.customer/internal->wire)})
