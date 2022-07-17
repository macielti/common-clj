(ns common-clj.component.config
  (:require [cheshire.core :as json]
            [clojure.tools.reader.edn :as edn]
            [com.stuartsierra.component :as component]
            [common-clj.keyword.core :as keyword.core]
            [schema.core :as s]))

(defmulti read-config-file
  (fn [_file-path type] type))

(s/defmethod read-config-file :json
  [file-path :- s/Str
   _type]
  (json/parse-string (slurp file-path)
                     keyword.core/str->keyword-kebab-case))

(s/defmethod read-config-file :edn
  [file-path :- s/Str
   _type]
  (edn/read-string (slurp file-path)))

(defrecord Config [path env type]
  component/Lifecycle
  (start [component]
    (let [config (-> (read-config-file path type)
                     env)]
      (merge component {:config (assoc config
                                       :current-env env)})))

  (stop [component]
    (assoc component :config nil)))

(defn new-config [path env type]
  (map->Config {:path path :env env :type type}))
