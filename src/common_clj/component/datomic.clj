(ns common-clj.component.datomic
  (:use [clojure pprint])
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d]))

(defrecord Datomic [config schemas]
  component/Lifecycle
  (start [component]
    (d/create-database (:datomic-uri config))
    (let [datomic-uri (:datomic-uri config)
          connection  (d/connect datomic-uri)]
      @(d/transact connection (flatten schemas))
      (merge component {:connection connection})))

  (stop [{:keys [connection] :as component}]
    (d/release connection)
    (assoc component :connection nil)))

(defn new-datomic [schemas]
  (map->Datomic {:schemas schemas}))
