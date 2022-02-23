(ns common-clj.time.core
  (:require [schema.core :as s])
  (:import (java.util Date)))

(s/defn now-datetime :- Date
  []
  (Date.))
