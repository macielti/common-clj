(ns common-clj.integrant-components.routes
  (:require [clojure.tools.logging :as log]
            [integrant.core :as ig]))

(defmethod ig/init-key ::routes
  [_ {:keys [routes]}]
  (log/info :starting ::routes)
  (into #{} routes))

(defmethod ig/halt-key! ::routes
  [_ _routes]
  (log/info :stopping ::routes))
