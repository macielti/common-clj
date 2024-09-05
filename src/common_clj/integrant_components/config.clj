(ns common-clj.integrant-components.config
  (:require [clojure.tools.reader.edn :as edn]
            [integrant.core :as ig]
            [schema.core :as s]
            [taoensso.timbre :as log]))

(s/defn config-file!
  "Expects a Path to an edn file"
  [file-path :- s/Str]
  (edn/read-string (slurp file-path)))

(defmethod ig/init-key ::config
  [_ {:keys [path env overrides]}]
  (log/info :starting ::config)
  (merge (-> (config-file! path) env)
         overrides
         {:current-env env}))

(defmethod ig/halt-key! ::config
  [_ _routes]
  (log/info :stopping ::config))
