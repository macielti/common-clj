(ns common-clj.component.routes
  (:require [com.stuartsierra.component :as component]))

(defrecord Routes [routes]
  component/Lifecycle
  (start [component]
    (assoc component :routes (into #{} routes)))

  (stop [component]
    (assoc component :routes nil)))

(defn new-routes [routes]
  (map->Routes {:routes routes}))
