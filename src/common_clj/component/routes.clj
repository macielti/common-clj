(ns common-clj.component.routes
  (:require [com.stuartsierra.component :as component]))

(defrecord Routes [routes datomic config]
  component/Lifecycle
  (start [component])

  (stop [component]
    (assoc component :routes nil)))

(defn new-routes [routes]
  (map->Routes {:routes routes}))
