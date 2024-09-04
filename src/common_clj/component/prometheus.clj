(ns common-clj.component.prometheus
  (:require [com.stuartsierra.component :as component]
            [iapetos.core :as prometheus]
            [iapetos.export :as export]
            [schema.core :as s]))

(s/defn ^:deprecated metrics
  [{headers                     :headers
    {:keys [prometheus config]} :components}]
  (if (= (get headers "authorization") (str "Bearer " (:prometheus-token config)))
    {:status 200
     :body   (export/text-format (:registry prometheus))}
    {:status 403
     :body   "Not Authorized"}))

(def ^:deprecated default-metrics
  [(prometheus/counter :http-request-response {:labels [:status :service :endpoint]})])

(defrecord ^:deprecated Prometheus [config metrics]
  component/Lifecycle
  (start ^:deprecated [component]
    (let [registry (-> (partial prometheus/register (prometheus/collector-registry))
                       (apply (concat metrics default-metrics)))]

      (merge component {:prometheus {:registry registry}})))

  (stop ^:deprecated [component]
    component))

(defn ^:deprecated new-prometheus [metrics]
  (map->Prometheus {:metrics metrics}))
