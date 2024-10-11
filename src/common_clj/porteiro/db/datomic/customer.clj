(ns common-clj.porteiro.db.datomic.customer
  (:require [common-clj.integrant-components.datomic :as component.datomic]
            [common-clj.porteiro.models.customer :as models.customer]
            [datomic.api :as d]
            [schema.core :as s]))

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

(s/defn lookup :- (s/maybe models.customer/Customer)
  [customer-id :- s/Uuid
   database]
  (some-> (d/q '[:find (pull ?e [*])
                 :in $ ?customer-id
                 :where [?e :customer/id ?customer-id]] database customer-id)
          ffirst
          (dissoc :db/id)))

(s/defn add-role!
  [customer-id :- s/Uuid
   role :- s/Keyword
   datomic]
  @(d/transact datomic [[:db/add [:customer/id customer-id] :customer/roles role]]))
