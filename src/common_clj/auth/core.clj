(ns common-clj.auth.core
  (:require [buddy.sign.jwt :as jwt]
            [java-time.api :as jt]
            [schema.core :as s]))

(s/defn ->token :- s/Str
  [map :- {s/Keyword s/Any}
   jwt-secret :- s/Str]
  (jwt/sign map
            jwt-secret
            {:exp (-> (jt/local-date-time (jt/zone-id "UTC"))
                      (jt/plus (jt/days 1))
                      (jt/sql-timestamp))}))
