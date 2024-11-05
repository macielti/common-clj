(ns common-clj.component.service
  (:require [com.stuartsierra.component :as component]
            [common-clj.io.interceptors :as io.interceptors]
            [io.pedestal.http :as http]
            [medley.core :as medley]))

(defrecord ^:deprecated Service [routes config http-client prometheus rate-limiter telegram-producer]
  component/Lifecycle
  (start ^:deprecated [component]
    (let [{{{:keys [host port]} :service} :config} config
          service-map {::http/routes          (:routes routes)
                       ::http/allowed-origins (constantly true)
                       ::http/host            host
                       ::http/port            port
                       ::http/type            :jetty
                       ::http/join?           false}
          components (medley/assoc-some {:config (:config config)}
                                        :http-client (:http-client http-client)
                                        :prometheus (:prometheus prometheus)
                                        :rate-limiter (:rate-limiter rate-limiter)
                                        :telegram-producer (:telegram-producer telegram-producer))]
      (assoc component :service (http/start (-> service-map
                                                http/default-interceptors
                                                (update ::http/interceptors concat (io.interceptors/common-interceptors
                                                                                    components))
                                                http/create-server)))))
  (stop ^:deprecated [component]
    (http/stop (:service component))
    (assoc component :service nil)))

(defn ^:deprecated new-service []
  (->Service {} {} {} {} {} {}))
