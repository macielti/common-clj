(ns common-clj.component.datomic
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d]
            [taoensso.timbre :as log]))

(defn mocked-datomic [datomic-schemas]
  (let [datomic-uri "datomic:mem://unit-tests"
        connection (do (d/create-database datomic-uri)
                       (d/connect datomic-uri))]
    @(d/transact connection (flatten datomic-schemas))
    connection))

(defrecord Datomic [config schemas]
  component/Lifecycle
  (start [component]
    (let [datomic-uri (or (-> config :config :datomic-uri)
                          "datomic:mem://integration-tests")
          connection (do (log/info ::database-creation (d/create-database datomic-uri))
                         (d/connect datomic-uri))]
      @(d/transact connection (flatten schemas))
      (assoc component :datomic connection)))

  (stop [{:keys [datomic] :as component}]
    (d/release datomic)
    (assoc component :datomic nil)))

(defn new-datomic [schemas]
  (map->Datomic {:schemas schemas}))
