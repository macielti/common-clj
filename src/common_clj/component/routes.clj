(ns common-clj.component.routes
  (:require [com.stuartsierra.component :as component]))

(defrecord ^:deprecated Routes [routes]
  component/Lifecycle
  (start ^:deprecated [component]
    (assoc component :routes (into #{} routes)))

  (stop ^:deprecated [component]
    (assoc component :routes nil)))

(defn ^:deprecated new-routes [routes]
  (map->Routes {:routes routes}))
