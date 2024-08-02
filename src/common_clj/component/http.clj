(ns common-clj.component.http
  (:require [cheshire.core :as json]
            [clj-http.client :as client]
            [com.stuartsierra.component :as component]
            [schema.core :as s]))

(s/defn ^:private authenticate-service! :- s/Str
  [auth-server-base-url :- s/Str
   username :- s/Str
   password :- s/Str]
  (-> (client/post (format "%s/users/auth" auth-server-base-url)
                   {:content-type :json
                    :body         {:username username
                                   :password password}})
      :body
      (json/decode true)
      :token))

(defrecord HTTP [config]
  component/Lifecycle
  (start [component]
    (let [{{{:keys [auth-server-base-url username password]} :service-authentication} :config} config]
      (assoc component :http {:authorization (authenticate-service! auth-server-base-url username password)})))

  (stop [component]
    (assoc component :http nil)))

(defn new-http []
  (->HTTP {}))
