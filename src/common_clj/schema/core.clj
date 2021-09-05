(ns common-clj.schema.core
  (:require [schema.core :as s]))

(defn loose-schema
  "Takes a schema and convert is to a loose schema, so we can validate schemas that fulfils
  definition and accept more properties than defined initially."
  [schema]
  (assoc schema s/Keyword s/Any))
