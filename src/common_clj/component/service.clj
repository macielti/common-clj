(ns common-clj.component.service
  (:require [com.stuartsierra.component :as component]
            [common-clj.io.interceptors :as io.interceptors]
            [io.pedestal.http :as http]
            [medley.core :as medley]))

(defrecord Service [routes config datomic datalevin postgresql rabbitmq-producer producer http-client prometheus rate-limiter telegram-producer]
  component/Lifecycle
  (start [component]
    (let [{{{:keys [host port]} :service} :config} config
          service-map {::http/routes          (:routes routes)
                       ::http/allowed-origins (constantly true)
                       ::http/host            host
                       ::http/port            port
                       ::http/type            :jetty
                       ::http/join?           false}
          components (medley/assoc-some {:config (:config config)}
                                        :producer (:producer producer)
                                        :rabbitmq-producer (:rabbitmq-producer rabbitmq-producer)
                                        :datomic (:datomic datomic)
                                        :datalevin (:datalevin datalevin)
                                        :postgresql (:postgresql postgresql)
                                        :http-client (:http-client http-client)
                                        :prometheus (:prometheus prometheus)
                                        :rate-limiter (:rate-limiter rate-limiter)
                                        :telegram-producer (:telegram-producer telegram-producer))]
      (assoc component :service (http/start (-> service-map
                                                http/default-interceptors
                                                (update ::http/interceptors concat (io.interceptors/common-interceptors
                                                                                    components))
                                                http/create-server)))))
  (stop [component]
    (http/stop (:service component))
    (assoc component :service nil)))

(defn new-service []
  (->Service {} {} {} {} {} {} {} {} {} {} {}))
