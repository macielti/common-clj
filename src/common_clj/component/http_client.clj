(ns common-clj.component.http-client
  (:require [com.stuartsierra.component :as component]
            [schema.core :as s]
            [clj-http.client :as client]))

(def method->request-fn
  {:post   client/post
   :get    client/get
   :delete client/delete})

(defmulti request!
          (fn [_ {:keys [current-env]}]
            current-env))

(s/defmethod request! :prod
             [{:keys [method url payload] :as _request-map}
              _http-client]
             (let [request-fn (method->request-fn method)]
               (request-fn url payload)))

(s/defmethod request! :test
             [{:keys [method url payload] :as request-map}
              {:keys [requests] :as _http-client}]
             (let [request-fn (method->request-fn method)]
               (swap! requests conj request-map)
               (request-fn url payload)))

(defn requests
  [{:keys [requests]}]
  @requests)

;TODO: Maybe in the future this can already incorporate the :http component so we have inter service authentication by default
(defrecord HttpClient [config]
  component/Lifecycle
  (start [component]
    (let [requests (atom [])]
      (assoc component :http-client {:requests    requests
                                     :current-env (-> config :config :current-env)})))

  (stop [component]
    (assoc component :http-client nil)))

(defn new-http-client []
  (->HttpClient {}))
