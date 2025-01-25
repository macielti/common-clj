(ns common-clj.traceability.core
  (:require [clojure.string :as str]
            [io.pedestal.interceptor :as pedestal.interceptor]
            [schema.core :as s]))

(def DEFAULT_CORRELATION_ID "DEFAULT")

(def ^:dynamic *correlation-id* (atom nil))

(defn reset-correlation-id! [] (reset! *correlation-id* nil))

(s/defn set-correlation-id! :- s/Str
  [correlation-id :- s/Str]
  (reset! *correlation-id* (clojure.string/upper-case correlation-id)))

(s/defn current-correlation-id! :- s/Str
  [] (or @*correlation-id* (reset! *correlation-id* DEFAULT_CORRELATION_ID)))

(s/defn correlation-id-appended! :- s/Str
  []
  (swap! *correlation-id* #(-> (or % DEFAULT_CORRELATION_ID)
                               (str "." (-> (random-uuid) str (str/split #"-") last))
                               clojure.string/upper-case)))

(s/defn correlation-id-from-request-context :- (s/maybe s/Str)
  [request-context]
  (-> (:headers request-context)
      (get "x-correlation-id" DEFAULT_CORRELATION_ID)
      clojure.string/upper-case))

(def with-correlation-id-http-interceptor
  (pedestal.interceptor/interceptor
   {:name  ::with-correlation-id-http-interceptor
    :enter (fn [{:keys [request] :as context}]
             (update context :bindings assoc #'*correlation-id* (atom (or (some-> request
                                                                                  correlation-id-from-request-context
                                                                                  set-correlation-id!)
                                                                          (set-correlation-id! DEFAULT_CORRELATION_ID)))))
    :leave (fn [context]
             (update context :bindings dissoc #'*correlation-id*))}))

(def with-correlation-id-rabbitmq-interceptor
  (pedestal.interceptor/interceptor
   {:name  ::with-correlation-id-rabbitmq-interceptor
    :enter (fn [{:keys [payload] :as context}]
             (update context :bindings assoc #'*correlation-id* (atom (or (some-> payload
                                                                                  :meta
                                                                                  :correlation-id
                                                                                  set-correlation-id!)
                                                                          (set-correlation-id! DEFAULT_CORRELATION_ID)))))
    :leave (fn [context]
             (update context :bindings dissoc #'*correlation-id*))}))

(s/defn with-correlation-id-job-interceptor
  [job-id :- s/Str]
  (pedestal.interceptor/interceptor
   {:name  ::with-correlation-id-job-interceptor
    :enter (fn [context]
             (update context :bindings assoc #'*correlation-id* (atom (set-correlation-id! job-id))))
    :leave (fn [context]
             (update context :bindings dissoc #'*correlation-id*))}))
