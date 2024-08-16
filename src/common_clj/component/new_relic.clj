(ns common-clj.component.new-relic
  (:require [cheshire.core :as json]
            [com.stuartsierra.component :as component]
            [common-clj.component.http-client :as component.http-client]
            [common-clj.traceability.core :as common-traceability]
            [medley.core :as medley]
            [taoensso.timbre :as timbre]))

(defn new-relic-http-appender
  [api-key service http-client & [opts]]
  {:enabled? true
   :async?   true
   :fn       (fn [data]
               (let [stacktrace-str (if-let [pr (:pr-stacktrace opts)]
                                      #(with-out-str (pr %))
                                      #(timbre/default-output-error-fn
                                        {:?err        %
                                         :output-opts {:stacktrace-fonts {}}}))
                     entry (medley/assoc-some {:cid       (common-traceability/current-correlation-id)
                                               :service   service
                                               :level     (str (name (:level data)))
                                               :log       (str (force (:msg_ data)))
                                               :namespace (str (:?ns-str data))
                                               :log-key   (-> data :vargs first str)
                                               :hostname  (str (force (:hostname_ data)))}
                                              :error (some-> (:?err data) str)
                                              :stacktrace (some-> (:?err data) (stacktrace-str)))]

                 (component.http-client/request! {:url     "https://log-api.newrelic.com/log/v1"
                                                  :method  :post
                                                  :payload {:headers {"Content-Type" "application/json"
                                                                      "Api-Key"      api-key}
                                                            :body    (json/encode entry)}}
                                                 http-client)))})

(defrecord NewRelic [config http-client]
  component/Lifecycle
  (start [component]
    (let [new-relic-api-key (-> config :config :new-relic-api-key)
          service (-> config :config :service-name)
          http-client (:http-client http-client)]
      (timbre/merge-config!
       {:appenders {:new-relic-http (new-relic-http-appender new-relic-api-key service http-client)}})
      component))

  (stop [component]
    component))

(defn new-new-relic []
  (->NewRelic {} {}))
