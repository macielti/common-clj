(ns common-clj.time.core
  (:require [java-time.api :as jt]
            [schema.core :as s])
  (:import (java.time LocalDate LocalDateTime ZoneOffset)
           (java.util Date TimeZone)))

(s/defn ^:deprecated local-datetime? :- s/Bool
  [value]
  (= (type value) LocalDateTime))

(s/defn ^:deprecated now :- LocalDateTime
  []
  (LocalDateTime/now (.toZoneId (TimeZone/getTimeZone "UTC"))))

(s/defn ^:deprecated date->local-datetime :- LocalDateTime
  [value]
  (-> (.toInstant value)
      (.atZone (.toZoneId (TimeZone/getTimeZone "UTC")))
      .toLocalDateTime))

(s/defn ^:deprecated date->local-date :- LocalDate
  [value :- Date]
  (-> (.toInstant value)
      (.atZone (.toZoneId (TimeZone/getTimeZone "UTC")))
      .toLocalDate))

(s/defn ^:deprecated local-datetime->date :- Date
  [value]
  (-> (.toInstant value ZoneOffset/UTC)
      Date/from))

(s/defn instant-now :- Date
  []
  (jt/instant))
