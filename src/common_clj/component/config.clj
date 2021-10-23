(ns common-clj.component.config
  (:require [com.stuartsierra.component :as component]
            [common-clj.keyword.core :as keyword.core]
            [cheshire.core :as json]))

(defrecord Config [path]
  component/Lifecycle
  (start [this]
    (let [config (json/parse-string (slurp path)
                                    keyword.core/str->keyword-kebab-case)]
      (assoc this :config config)))

  (stop [this]
    (assoc this :config nil)))

(defn new-config [path]
  (->Config {:path path}))
