(ns common-clj.time.parser.core
  (:require [schema.core :as s])
  (:import (java.time LocalDate)
           (java.time.format DateTimeFormatter)
           (java.util Date)
           (java.text SimpleDateFormat)))

(s/defn str->local-date :- LocalDate
  [in-format :- s/Str
   date :- s/Str]
  (cond (boolean (re-find #"Hoje" date)) (LocalDate/now)
        (boolean (re-find #"Ontem" date)) (doto (LocalDate/now)
                                            (.minusDays 1))
        :else (LocalDate/parse date (DateTimeFormatter/ofPattern in-format))))

(s/defn local-date->str :- s/Str
  [out-format :- s/Str
   date :- LocalDate]
  (let [formatter (DateTimeFormatter/ofPattern out-format)]
    (.format date formatter)))

(s/defn date->wire :- s/Str
  "Convert Date object to ISO-8601 string"
  [date :- Date]
  (.format (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ssZ") date))
