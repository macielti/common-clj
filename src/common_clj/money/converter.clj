(ns common-clj.money.converter
  (:require [schema.core :as s])
  (:import (clojure.lang BigInt)))

(s/defn ->cents :- BigInt
  [value]
  (-> (* value 100)
      bigint))
