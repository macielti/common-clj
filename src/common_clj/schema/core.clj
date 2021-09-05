(ns common-clj.schema.core
  (:require [schema.core :as s]))

(s/defn loose-schema [schema]
  (assoc schema s/Keyword s/Any))
