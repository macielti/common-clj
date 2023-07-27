(ns common-clj.component.datalevin
  (:require [com.stuartsierra.component :as component]
            [datalevin.core :as datalevin]))

(defrecord Datalevin [config schema]
  component/Lifecycle
  (start [component]
    (let [env (-> config :config :current-env)
          database-uri (case env
                         :test (datalevin.util/tmp-dir (str "query-or-" (random-uuid)))
                         :default (-> config :config :database-uri))
          database (datalevin/get-conn database-uri schema)]

      (merge component {:datalevin database})))

  (stop [{:keys [datalevin]}]
    (datalevin/close datalevin)))

(defn new-datalevin
  [schema]
  (map->Datalevin {:schema schema}))
