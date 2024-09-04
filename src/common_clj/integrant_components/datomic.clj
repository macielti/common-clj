(ns common-clj.integrant-components.datomic
  (:require [datomic.api :as d]
            [integrant.core :as ig]
            [taoensso.timbre :as log]))

(defn mocked-datomic [datomic-schemas]
  (let [datomic-uri "datomic:mem://unit-tests"
        connection (do (d/create-database datomic-uri)
                       (d/connect datomic-uri))]
    @(d/transact connection (flatten datomic-schemas))
    connection))

(defmethod ig/init-key ::datomic
  [_ {:keys [components schemas]}]
  (log/info :starting ::datomic)
  (let [datomic-uri (or (-> components :config :datomic-uri)
                        "datomic:mem://integration-tests")
        connection (do (log/info ::database-creation (d/create-database datomic-uri))
                       (d/connect datomic-uri))]
    @(d/transact connection (flatten schemas))
    connection))

(defmethod ig/halt-key! ::datomic
  [_ datomic]
  (log/info :stopping ::datomic)
  (d/release datomic))