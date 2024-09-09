(ns common-clj.integrant-components.aws-auth
  (:require [amazonica.core :as aws]
            [integrant.core :as ig]
            [io.pedestal.log :as log]))

(defmethod ig/init-key ::aws-auth
  [_ {:keys [components]}]
  (log/info :starting ::aws-auth)
  (aws/defcredential (-> components :config :aws-credentials :access-key)
    (-> components :config :aws-credentials :secret-key)
    (-> components :config :aws-credentials :endpoint)))

(defmethod ig/halt-key! ::aws-auth
  [_ _aws-auth]
  (log/info :stopping ::aws-auth))
