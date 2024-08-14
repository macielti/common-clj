(ns common-clj.traceability.core
  (:require [clojure.string :as str]
            [schema.core :as s]))

(def ^:dynamic *correlation-id* nil)

(s/defn correlation-id-appended :- s/Str
  [correlation-id :- s/Str]
  {:pre [(not-empty correlation-id)]}
  (-> (str correlation-id "." (-> (random-uuid) str (str/split #"-") last))
      clojure.string/upper-case))

(s/defn current-correlation-id :- s/Str
  []
  (or *correlation-id* (correlation-id-appended "DEFAULT")))

(s/defn current-correlation-id-from-request-context :- (s/maybe s/Str)
  [request-context]
  (-> (:headers request-context)
      (get "x-correlation-id" (current-correlation-id))
      clojure.string/upper-case))

(s/defn http-with-correlation-id
  [http-request-handler-fn]
  (s/fn [request-context]
    (binding [*correlation-id* (-> (current-correlation-id-from-request-context request-context)
                                   correlation-id-appended)]
      (http-request-handler-fn request-context))))

(s/defn job-with-correlation-id
  [job-handler-fn
   job-id :- s/Str]
  (s/fn [as-of params instance]
    (binding [*correlation-id* (correlation-id-appended job-id)]
      (job-handler-fn as-of params instance))))