(ns common-clj.traceability.core
  (:require [schema.core :as s]))

(def ^:dynamic *correlation-id* nil)

(s/defn current-correlation-id-from-request-context :- (s/maybe s/Str)
  [request-context]
  (-> (:headers request-context)
      (get "x-correlation-id" "DEFAULT")
      clojure.string/upper-case))

;TODO: Add check for empty strings for correlation-id input args (Should trow a exception)
(s/defn correlation-id-appended :- s/Str
  [correlation-id :- s/Str]
  {:pre [(not-empty correlation-id)]}
  (-> (str correlation-id "." (random-uuid))
      clojure.string/upper-case))

;TODO: Add missing integration test for that fn
(s/defn with-correlation-id
  [http-request-handler-fn]
  (s/fn [request-context]
    (binding [*correlation-id* (-> (current-correlation-id-from-request-context request-context)
                                   correlation-id-appended)]
      (http-request-handler-fn request-context))))
