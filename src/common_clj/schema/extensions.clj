(ns common-clj.schema.extensions
  (:require [schema.core :as s])
  (:import (java.util.regex Pattern)))

(s/defn custom-string-pattern
  [pattern :- Pattern]
  (s/constrained s/Str #(re-matches pattern %)))

(s/defschema ^:deprecated LocalDateWire
  "Example: '2024-09-07'"
  (s/constrained s/Str #(re-matches #"^\d{4}-\d{2}-\d{2}$" %)))

(s/defschema ^:deprecated LocalDateTimeWire
  "Example: '2024-11-21T04:56:24.605402' or '2024-11-21T04:56'"
  (s/constrained s/Str #(or (re-matches #"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{6,9}$" %)
                            (re-matches #"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$" %))))

(def uuid-regex #"[0-9A-Fa-f]{8}(-[0-9A-Fa-f]{4}){3}-[0-9A-Fa-f]{12}")
(defn uuid-string? [str]
  (and (string? str)
       (some? (re-matches uuid-regex str))))

(s/defschema UuidWire
  "Example: '9c97de3b-ac65-44b2-ba4f-b65f8778e514'"
  (s/constrained s/Str uuid-string?))

(s/defschema CalendarDateWire
  "Example: '2024-09-07'"
  (s/constrained s/Str #(re-matches #"^\d{4}-\d{2}-\d{2}$" %)))

(s/defschema InstantWire
  (s/constrained s/Str #(or (re-matches #"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+Z$" %)
                            (re-matches #"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z$" %))))

(s/defschema PositiveInt
  "An integer greater than 0. Don't accept BigInt"
  (s/constrained s/Int pos-int?))

(s/defschema NonNegativeInt
  "An integer greater than or equal to 0. Don't accept BigInt"
  (s/constrained s/Int #(or (= 0 %) (pos-int? %))))

