(ns common-clj.traceability.core
  (:require [schema.core :as s]))

(def ^:dynamic *correlation-id* nil)

(s/defn current-correlation-id :- (s/maybe s/Str)
  [http-request-handler-fn]
  (-> (:headers http-request-handler-fn)
      (get-in "x-correlation-id" "DEFAULT")))

(s/defn with-correlation-id
  [http-request-handler-fn]
  (s/fn [request-context]
    (binding [])))
