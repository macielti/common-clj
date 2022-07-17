(ns common-clj.component.service
  (:require [com.stuartsierra.component :as component]
            [common-clj.io.interceptors :as io.interceptors]
            [io.pedestal.http :as http]
            [plumbing.core :as plumbing]))

(defrecord Service [routes config datomic producer]
  component/Lifecycle
  (start [component]
    (let [{{{:keys [host port]} :service} :config} config
          service-map {::http/routes (:routes routes)
                       ::http/host   host
                       ::http/port   port
                       ::http/type   :jetty
                       ::http/join?  false}
          components  (plumbing/assoc-when {}
                                           :producer (:producer producer)
                                           :config (:config config)
                                           :datomic (:datomic datomic))]
      (assoc component :service (http/start (-> service-map
                                                http/default-interceptors
                                                (update ::http/interceptors concat (io.interceptors/common-interceptors
                                                                                    components))
                                                http/create-server)))))
  (stop [component]
    (http/stop (:service component))
    (assoc component :service nil)))

(defn new-service []
  (->Service {} {} {} {}))
