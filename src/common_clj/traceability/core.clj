(ns common-clj.traceability.core
  (:require [schema.core :as s]))

(def ^:dynamic *correlation-id* nil)

;TODO: Add missing unit test for that fn
(s/defn current-correlation-id-from-request-context :- (s/maybe s/Str)
  [request-context]
  (-> (:headers request-context)
      (get-in "x-correlation-id" "DEFAULT")))

;TODO: Add missing unit test for that fn
(s/defn correlation-id-appended :- s/Str
  [correlation-id]
  (-> (str correlation-id "." (random-uuid))
      clojure.string/upper-case))

;TODO: Add missing integration test for that fn
(s/defn with-correlation-id
  [http-request-handler-fn]
  (s/fn [request-context]
    (binding [*correlation-id* (-> (current-correlation-id-from-request-context request-context)
                                   correlation-id-appended)]
      (http-request-handler-fn request-context))))
