(ns common-clj.component.datomic
  (:use [clojure pprint])
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d]))

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
