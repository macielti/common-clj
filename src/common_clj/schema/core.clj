(ns common-clj.schema.core
  (:require [schema.core :as s]
            [clojure.walk :as walk])
  (:import (clojure.lang PersistentArrayMap)))

(defn loose-schema
  "Takes a schema and converts it to a loose schema, so we can validate schemas that fulfils
  definition and accept more properties than defined initially."
  [schema]
  (walk/postwalk #(if (= (type %) PersistentArrayMap)
                    (assoc % s/Keyword s/Any)
                    %)
                 schema))
