(ns common-clj.integrant-components.routes
  (:require [integrant.core :as ig]
            [taoensso.timbre :as log]))

(defmethod ig/init-key :common-clj.integrant-components/routes
  [_
   {:keys [routes]}]
  (log/info :starting-routes)
  (into #{} routes))

(defmethod ig/halt-key! :common-clj.integrant-components/routes
  [_ _routes]
  (log/info :stopping-routes))