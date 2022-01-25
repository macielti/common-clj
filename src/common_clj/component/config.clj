(ns common-clj.component.config
  (:require [com.stuartsierra.component :as component]
            [common-clj.keyword.core :as keyword.core]
            [cheshire.core :as json]))

(defrecord Config [path env]
  component/Lifecycle
  (start [component]
    (let [config (-> (json/parse-string (slurp path)
                                        keyword.core/str->keyword-kebab-case)
                     env)]
      (merge component {:config (assoc config
                                  :current-env env)})))

  (stop [component]
    (assoc component :config nil)))

(defn new-config [path env]
  (map->Config {:path path :env env}))
