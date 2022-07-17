(ns common-clj.component.datalevin
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [datalevin.core :as datalevin]
            [schema.core :as s]))

(s/defn mocked-datalevin-component
  [schema]
  (let [database-uri "/tmp/datalevin"]
    (try (io/delete-file (str database-uri "/data.mdb"))
         (io/delete-file (str database-uri "/lock.mdb"))
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
