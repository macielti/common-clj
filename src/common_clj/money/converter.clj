(ns common-clj.money.converter
  (:require [schema.core :as s]))

(s/defn ->cents :- BigInteger
  [value]
  (-> (* value 100)
      bigint))
