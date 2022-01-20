(ns common-clj.component.datomic
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d]))

(defn mocked-datomic [datomic-schemas]
  (let [datomic-uri "datomic:mem://mocked"
        connection  (do (d/create-database datomic-uri)
                        (d/connect datomic-uri))]
    @(d/transact connection (flatten datomic-schemas))
    connection))

(defrecord Datomic [config schemas]
  component/Lifecycle
  (start [component]
    (let [datomic-uri (-> config :config :datomic-uri)
          connection  (do (d/create-database datomic-uri)
                          (d/connect datomic-uri))]
      @(d/transact connection (flatten schemas))
      (assoc component :datomic {:connection connection})))

  (stop [{{:keys [connection]} :datomic :as component}]
    (d/release connection)
    (assoc component :datomic nil)))

(defn new-datomic [schemas]
  (map->Datomic {:schemas schemas}))
