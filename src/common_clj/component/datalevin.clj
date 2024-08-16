(ns common-clj.component.datalevin
  (:require [com.stuartsierra.component :as component]
            [datalevin.core :as datalevin]
            [datalevin.util]))

(defrecord Datalevin [config schema]
  component/Lifecycle
  (start [component]
    (let [config-content (:config config)
          env (-> config-content :current-env)
          database-uri (case env
                         :test (datalevin.util/tmp-dir (str "query-or-" (random-uuid)))
                         (-> config :config :database-uri))
          database (if database-uri
                     (datalevin/get-conn database-uri schema)
                     (throw (ex-info "'database-uri' Config property is not defined"
                                     {:env            env
                                      :config-content config-content
                                      :database-uri   database-uri})))]

      (merge component {:datalevin database})))

  (stop [{:keys [datalevin]}]
    (datalevin/close datalevin)))

(defn new-datalevin
  [schema]
  (map->Datalevin {:schema schema}))
