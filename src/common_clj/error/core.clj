(ns common-clj.error.core
  (:require [schema.core :as s]))

(s/defn http-friendly-exception
  "https://www.baeldung.com/rest-api-error-handling-best-practices"
  [status-code :- s/Int
   error :- s/Str
   message :- s/Str
   detail :- s/Str]
  (throw (ex-info (format "%s - %s" status-code error)
                  {:status  status-code
                   :error   error
                   :message message
                   :detail  detail})))
