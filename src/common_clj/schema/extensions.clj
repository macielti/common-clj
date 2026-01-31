(ns common-clj.schema.extensions
  (:require [clj-uuid]
            [java-time.api :as jt]
            [schema.core :as s]))

(s/defn custom-formated-wire-instant
  [format :- s/Str]
  (s/constrained s/Str #(jt/instant format %)))

(s/defschema LocalDateWire
  "Example: '2024-09-07'"
  (s/constrained s/Str #(re-matches #"^\d{4}-\d{2}-\d{2}$" %)))

(s/defschema LocalDateTimeWire
  "Example: '2024-11-21T04:56:24.605402' or '2024-11-21T04:56'"
  (s/constrained s/Str #(or (re-matches #"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{6,9}$" %)
                            (re-matches #"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$" %))))

(s/defschema UuidWire
  "Example: '9c97de3b-ac65-44b2-ba4f-b65f8778e514'"
  (s/constrained s/Str clj-uuid/uuid-string?))
