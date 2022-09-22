(ns common-clj.component.http
  (:require [com.stuartsierra.component :as component]
            [schema.core :as s]
            [clj-http.conn-mgr :as conn]
            [clj-http.client :as client])
  (:import (org.apache.http.message BasicHeader)))

(s/defn ^:private authenticate-service!
  [auth-server-base-url :- s/Str
   username :- s/Str
   password :- s/Str]
  (let [connection-manager (conn/make-reusable-conn-manager {:insecure? true})
        {{:keys [token]} :body} (client/post (format "%s/users/auth" auth-server-base-url)
                                             {:content-type       :json
                                              :connection-manager connection-manager
                                              :body               {:username username
                                                                   :password password}})
        {http-client :http-client} (client/get (format "%s/users/contacts" auth-server-base-url)
                                               {:connection-manager connection-manager
                                                :http-builder-fns   [(fn [builder request]
                                                                       (.setDefaultHeaders builder
                                                                                           (:http-builder-fns request)))]
                                                :default-headers    [(BasicHeader. "Authorization" token)]})]
    http-client))

(defrecord HTTP [config]
  component/Lifecycle
  (start [component]
    (let [{{{:keys [auth-server-base-url username password]} :service-authentication} :config} config]
      (assoc component :http {:http-client (authenticate-service! auth-server-base-url username password)})))

  (stop [component]
    (assoc component :http nil)))

(defn new-http []
  (->HTTP {}))