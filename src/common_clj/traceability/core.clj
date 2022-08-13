(ns common-clj.traceability.core
  (:require [schema.core :as s]))

(def ^:dynamic *correlation-id* nil)

(s/defn ^:private current-correlation-id :- (s/maybe s/Str)
  [http-request-handler-fn]
  (-> (:headers http-request-handler-fn)
      (get-in "x-correlation-id" "DEFAULT")))

(s/defn correlation-id-appended :- s/Str
  [correlation-id]
  (-> (str correlation-id "." (random-uuid))
      clojure.string/upper-case))

(s/defn with-correlation-id
  [http-request-handler-fn]
  (s/fn [request-context]
    (binding [*correlation-id* (-> (current-correlation-id request-context)
                                   correlation-id-appended)]
      (http-request-handler-fn request-context))))
