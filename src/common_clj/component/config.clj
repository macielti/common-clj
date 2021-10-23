(ns common-clj.component.config
  (:require [com.stuartsierra.component :as component]
            [common-clj.keyword.core :as keyword.core]
            [cheshire.core :as json]))

(defrecord Config [path env]
  component/Lifecycle
  (start [this]
    (let [config (-> (json/parse-string (slurp path)
                                        keyword.core/str->keyword-kebab-case)
                     env)]
      (merge this config)))

  (stop [this]
    (assoc this :config nil)))

(defn new-config [path env]
  (map->Config {:path path :env env}))
