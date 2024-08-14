(ns common-clj.component.prometheus
  (:require [com.stuartsierra.component :as component]
            [iapetos.core :as prometheus]
            [iapetos.export :as export]
            [schema.core :as s]))

(s/defn metrics
  [{headers                     :headers
    {:keys [prometheus config]} :components}]
  (if (= (get headers "authorization") (str "Bearer " (:prometheus-token config)))
    {:status 200
     :body   (export/text-format (:registry prometheus))}
    {:status 403
     :body   "Not Authorized"}))

(defrecord Prometheus [config metrics]
  component/Lifecycle
  (start [component]
    (let [registry (-> (partial prometheus/register (prometheus/collector-registry))
                       (apply metrics))]

      (merge component {:prometheus {:registry registry}})))

  (stop [component]
    component))

(defn new-prometheus [metrics]
  (map->Prometheus {:metrics metrics}))