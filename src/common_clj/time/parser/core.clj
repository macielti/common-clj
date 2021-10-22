(ns common-clj.time.parser.core
  (:require [schema.core :as s])
  (:import (java.time LocalDate)
           (java.time.format DateTimeFormatter)))

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
    (doto date
      (.format formatter))))
