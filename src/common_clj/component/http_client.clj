(ns common-clj.component.http-client
  (:require [camel-snake-kebab.core :as camel-snake-kebab]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [com.stuartsierra.component :as component]
            [iapetos.core :as prometheus]
            [medley.core :as medley]
            [schema.core :as s]
            [taoensso.timbre :as log]))

(def ^:deprecated method->request-fn
  {:post   client/post
   :get    client/get
   :patch  client/patch
   :delete client/delete})

(defmulti ^:deprecated request!
  (fn [_ {:keys [current-env]}]
    current-env))

(s/defmethod request! :prod
  [{:keys [method url payload endpoint-id] :as _request-map}
   {:keys [prometheus-registry service] :as _http-client}]
  (try (let [request-fn (method->request-fn method)
             http-response (request-fn url payload)]
         (when prometheus-registry
           (prometheus/inc prometheus-registry :http-request-response {:status   (:status http-response)
                                                                       :service  service
                                                                       :endpoint (camel-snake-kebab/->snake_case_string endpoint-id)}))
         http-response)
       (catch Exception ex
         (when prometheus-registry
           (prometheus/inc prometheus-registry :http-request-response {:status   (:status (ex-data ex))
                                                                       :service  service
                                                                       :endpoint (camel-snake-kebab/->snake_case_string endpoint-id)}))
         (log/error ex)
         (throw ex))))

(s/defmethod request! :test
  [{:keys [method url payload] :as request-map}
   {:keys [requests] :as _http-client}]
  (let [request-fn (method->request-fn method)]
    (swap! requests conj request-map)
    (request-fn url payload)))

(defn ^:deprecated requests
  [{:keys [requests]}]
  (map (fn [request]
         (medley/update-existing-in request [:payload :body] #(json/decode % true))) @requests))

;TODO: Maybe in the future this can already incorporate the :http component so we have inter service authentication by default
(defrecord ^:deprecated HttpClient [config prometheus]
  component/Lifecycle
  (start ^:deprecated [component]
    (let [requests (atom [])]
      (assoc component :http-client (medley/assoc-some {:requests    requests
                                                        :service     (-> config :config :service-name)
                                                        :current-env (-> config :config :current-env)}
                                                       :prometheus-registry (-> prometheus :prometheus :registry)))))

  (stop ^:deprecated [component]
    (assoc component :http-client nil)))

(defn ^:deprecated new-http-client []
  (->HttpClient {} {}))
