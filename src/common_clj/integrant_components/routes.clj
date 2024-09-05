(ns common-clj.integrant-components.routes
  (:require [integrant.core :as ig]
            [taoensso.timbre :as log]))

(defmethod ig/init-key ::routes
  [_ {:keys [routes]}]
  (log/info :starting ::routes)
  (into #{} routes))

(defmethod ig/halt-key! ::routes
  [_ _routes]
  (log/info :stopping ::routes))
