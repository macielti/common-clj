(ns common-clj.porteiro.db.datomic.customer
  (:require [datomic.api :as d]
            [schema.core :as s]
            [common-clj.porteiro.models.customer :as models.customer]
            [common-clj.integrant-components.datomic :as component.datomic]))

(s/defn insert! :- models.customer/Customer
  [customer :- models.customer/Customer
   datomic]
  (-> (component.datomic/transact-and-lookup-entity! :customer/id customer datomic)
      :entity))

(s/defn by-username :- (s/maybe models.customer/Customer)
  [username :- s/Str
   database]
  (some-> (d/q '[:find (pull ?customer [*])
                 :in $ ?username
                 :where [?customer :customer/username ?username]] database username)
          ffirst
          (dissoc :db/id)))
