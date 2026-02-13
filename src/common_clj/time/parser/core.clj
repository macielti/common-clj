(ns common-clj.time.parser.core
  (:require [common-clj.schema.extensions]
            [java-time.api :as jt]
            [schema.core :as s])
  (:import (java.time Instant LocalDate ZoneOffset)
           (java.time.format DateTimeFormatter)
           (java.util Date)))

(s/defn ^:deprecated str->local-date :- LocalDate
  [in-format :- s/Str
   date :- s/Str]
  (cond (boolean (re-find #"Hoje" date)) (LocalDate/now)
        (boolean (re-find #"Ontem" date)) (doto (LocalDate/now)
                                            (.minusDays 1))
        :else (LocalDate/parse date (DateTimeFormatter/ofPattern in-format))))

(s/defn ^:deprecated local-date->str :- s/Str
  [out-format :- s/Str
   date :- LocalDate]
  (let [formatter (DateTimeFormatter/ofPattern out-format)]
    (.format date formatter)))

(s/defn ^:deprecated date->wire :- s/Str
  "Convert UTC Date object to ISO-8601 string"
  [date :- Date]
  (-> (.atOffset (.toInstant date) ZoneOffset/UTC)
      (.format DateTimeFormatter/ISO_DATE_TIME)))

(s/defn wire->instant :- Instant
  [wire-instant :- common-clj.schema.extensions/InstantWire]
  (jt/instant wire-instant))

(s/defn instant->wire :- common-clj.schema.extensions/InstantWire
  [instant :- Instant]
  (str instant))

(s/defn instant->legacy-date :- Date
  [instant :- Instant]
  (jt/java-date instant))
