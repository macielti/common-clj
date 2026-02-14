(ns common-clj.time.util
  (:require [java-time.api :as jt]
            [schema.core :as s])
  (:import (java.time Instant)))

(s/defn instant-now :- Instant
  []
  (jt/instant))
