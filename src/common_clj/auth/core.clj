(ns common-clj.auth.core
  (:require [schema.core :as s]
            [clj-time.core :as t]
            [buddy.sign.jwt :as jwt]
            [clj-time.coerce :as c]))

(s/defn ->token :- s/Str
  [map :- {s/Keyword s/Any}
   jwt-secret :- s/Str]
  (jwt/sign map
            jwt-secret
            {:exp (-> (t/plus (t/now) (t/days 1))
                      c/to-timestamp)}))
