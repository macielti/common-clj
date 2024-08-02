(ns common-clj.schema.core
  (:require [clojure.walk :as walk]
            [common-clj.keyword.core :as common-keyword]
            [schema-tools.walk :as st-walk]
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

(s/defn un-namespaced :- s/Schema
  "Recursively un-namespace schemas"
  [schema :- s/Schema]
  (st-walk/postwalk (fn [x]
                      (if (keyword? x)
                        (common-keyword/un-namespaced x)
                        x)) schema))
