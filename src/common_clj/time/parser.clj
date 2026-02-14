(ns common-clj.time.parser
  (:require [common-clj.schema.extensions :as schema.extensions]
            [java-time.api :as jt]
            [schema.core :as s])
  (:import (java.time Instant)
           (java.util Date)))

(s/defn wire->instant :- Instant
  [wire-instant :- schema.extensions/InstantWire]
  (jt/instant wire-instant))

(s/defn instant->wire :- schema.extensions/InstantWire
  [instant :- Instant]
  (str instant))

(s/defn instant->legacy-date :- Date
  [instant :- Instant]
  (jt/java-date instant))

(s/defn legacy-date->instant :- Instant
  [legacy-date :- Date]
  (.toInstant ^Date legacy-date))