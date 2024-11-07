(ns common-clj.integrant-components.service
  (:require [common-clj.io.interceptors :as io.interceptors]
            [integrant.core :as ig]
            [io.pedestal.http :as http]
            [clojure.tools.logging :as log]))

(defmethod ig/init-key ::service
  [_ {:keys [components]}]
  (log/info :starting ::service)
  (http/start (-> {::http/routes          (:routes components)
                   ::http/allowed-origins (constantly true)
                   ::http/host            (-> components :config :service :host)
                   ::http/port            (-> components :config :service :port)
                   ::http/type            :jetty
                   ::http/join?           false}
                  http/default-interceptors
                  (update ::http/interceptors concat (io.interceptors/common-interceptors components))
                  http/create-server)))

(defmethod ig/halt-key! ::service
  [_ service]
  (log/info :stopping ::service)
  (http/stop service))
