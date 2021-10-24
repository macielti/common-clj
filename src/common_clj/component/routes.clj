(ns common-clj.component.routes
  (:use [clojure pprint])
  (:require [com.stuartsierra.component :as component]))

(defrecord Routes [routes datomic config]
  component/Lifecycle
  (start [component]
    (assoc component :routes (into #{} routes)))

  (stop [component]
    (assoc component :routes nil)))

(defn new-routes [routes]
  (map->Routes {:routes routes}))