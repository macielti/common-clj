(ns common-clj.component.datalevin
  (:require [com.stuartsierra.component :as component]
            [datalevin.core :as datalevin]
            [clojure.java.io :as io]
            [schema.core :as s]))

(s/defn mocked-datalevin-component
  [schema]
  (let [database-uri "/tmp/datalevin"]
    (try (do (io/delete-file (str database-uri "/data.mdb"))
             (io/delete-file (str database-uri "/lock.mdb")))
         (catch Exception _))
    (datalevin/get-conn database-uri schema)))

(defrecord Datalevin [config schemas]
  component/Lifecycle
  (start [component]
    (let [{{:keys [database-uri]} :config} config
          database (datalevin/get-conn database-uri)]
      (merge component {:database database})))

  (stop [{:keys [database]}]
    (datalevin/close database)))

(defn new-datalevin
  [schemas]
  (map->Datalevin {:schemas schemas}))
