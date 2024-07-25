(ns common-clj.schema.extensions
  (:require [schema.core :as s]))

(s/defschema LocalDateWire
  "Example: '2024-09-07'"
  (s/constrained s/Str #(re-matches #"^\d{4}-\d{2}-\d{2}T\d{2}$" %)))
