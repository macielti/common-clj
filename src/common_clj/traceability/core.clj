(ns common-clj.traceability.core
  (:require [clojure.string :as str]
            [io.pedestal.interceptor :as pedestal.interceptor]
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

(def with-correlation-id-interceptor
  (pedestal.interceptor/interceptor
   {:name  ::with-correlation-id-interceptor
    :enter (fn [{:keys [request] :as context}]
             (update context :bindings assoc #'*correlation-id* (-> (current-correlation-id-from-request-context request)
                                                                    correlation-id-appended)))
    :leave (fn [context]
             (update context :bindings dissoc #'*correlation-id*))}))

(s/defn job-with-correlation-id
  [job-handler-fn
   job-id :- s/Str]
  (s/fn [as-of params instance]
    (binding [*correlation-id* (correlation-id-appended job-id)]
      (job-handler-fn as-of params instance))))
