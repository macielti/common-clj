(ns common-clj.component.config
  (:require [com.stuartsierra.component :as component]
            [common-clj.keyword.core :as keyword.core]
            [cheshire.core :as json]
            [schema.core :as s]))

(defrecord Config [path env]
  component/Lifecycle
  (start [component]
    (let [config (-> (json/parse-string (slurp path)
                                        keyword.core/str->keyword-kebab-case)
                     env)]
      (merge component {:config (assoc config
                                  :env env
                                  :path path)})))

  (stop [component]
    (assoc component :config nil)))

(defn new-config [path env]
  (map->Config {:path path :env env}))

(s/defn insert!
  [write-to])
