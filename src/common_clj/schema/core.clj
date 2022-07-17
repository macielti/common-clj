(ns common-clj.schema.core
  (:require [clojure.walk :as walk]
            [schema.core :as s])
  (:import (clojure.lang PersistentArrayMap)))

(defn loose-schema
  "Takes a schema and converts it to a loose schema, so we can validate schemas that fulfils
  definition and accept more properties than defined initially."
  [schema]
  (walk/postwalk #(if (= (type %) PersistentArrayMap)
                    (assoc % s/Keyword s/Any)
                    %)
                 schema))
