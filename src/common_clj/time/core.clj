(ns common-clj.time.core
  (:require [schema.core :as s])
  (:import (java.time LocalDateTime ZoneOffset)
           (java.util Date TimeZone)))

(s/defn ^:deprecated now-datetime :- Date
  []
  (Date.))

(s/defn local-datetime? :- s/Bool
  [value]
  (= (type value) LocalDateTime))

(s/defn now :- LocalDateTime
  []
  (LocalDateTime/now (.toZoneId (TimeZone/getTimeZone "UTC"))))

(s/defn date->local-datetime :- LocalDateTime
  [value]
  (-> (.toInstant value)
      (.atZone (.toZoneId (TimeZone/getTimeZone "UTC")))
      .toLocalDateTime))

(s/defn local-datetime->date :- Date
  [value]
  (-> (.toInstant value ZoneOffset/UTC)
      Date/from))