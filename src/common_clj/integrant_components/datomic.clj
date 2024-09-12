(ns common-clj.integrant-components.datomic
  (:require [datomic.api :as d]
            [integrant.core :as ig]
            [schema.core :as s]
            [taoensso.timbre :as log]
            [diehard.core :as dh])
  (:import (datomic.db Db)))

(s/defn transact-and-lookup-entity! :- {:entity   (s/pred map?)
                                        :db-after Db}
  [identity-key :- s/Keyword
   entity :- (s/pred map?)
   connection]
  (let [{:keys [db-after]} @(d/transact connection [entity])
        entity' (-> (d/q '[:find (pull ?entity [*])
                           :in $ ?identity-key ?entity-identity-id
                           :where [?entity ?identity-key ?entity-identity-id]] db-after identity-key (identity-key entity))
                    ffirst
                    (dissoc :db/id))]
    (when-not entity'
      (throw (ex-info "Entity not found after transacting it" {:entity entity})))
    {:entity   entity'
     :db-after db-after}))

(defn mocked-datomic [datomic-schemas]
  (let [datomic-uri (str "datomic:mem://" (random-uuid))
        connection (do (d/create-database datomic-uri)
                       (d/connect datomic-uri))]
    @(d/transact connection (flatten datomic-schemas))
    connection))

(defmethod ig/init-key ::datomic
  [_ {:keys [components schemas]}]
  (log/info :starting ::datomic)
  (let [datomic-uri (or (-> components :config :datomic-uri)
                        (str "datomic:mem://" (random-uuid)))
        connection (dh/with-retry {:retry-on    Exception
                                   :max-retries 3}
                     (log/info ::database-created? (d/create-database datomic-uri))
                     (d/connect datomic-uri))]
    @(d/transact connection (flatten schemas))
    connection))

(defmethod ig/halt-key! ::datomic
  [_ datomic]
  (log/info :stopping ::datomic)
  (d/release datomic))
