(ns common-clj.io.interceptors
  (:require [common-clj.error.core :as common-error]
            [humanize.schema :as h]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.interceptor :as pedestal.interceptor]
            [io.pedestal.interceptor.error :as error]
            [schema.core :as s]
            [taoensso.timbre :as log])
  (:import (clojure.lang ExceptionInfo)))

(def error-handler-interceptor
  (error/error-dispatch [ctx ex]
                        [{:exception-type :clojure.lang.ExceptionInfo}]
                        (let [{:keys [status error message detail]} (ex-data ex)]
                          (assoc ctx :response {:status status
                                                :body   {:error   error
                                                         :message message
                                                         :detail  detail}}))

                        :else
                        (do (log/error ex)
                            (assoc ctx :response {:status 500 :body {:error   "unexpected-server-error"
                                                                     :message "Internal Server Error"
                                                                     :detail  "Internal Server Error"}}))))

(defn components-interceptor [system-components]
  (pedestal.interceptor/interceptor {:name  ::components-interceptor
                                     :enter (fn [context]
                                              (assoc-in context [:request :components] system-components))}))

(defn schema-body-in-interceptor [schema]
  (pedestal.interceptor/interceptor {:name  ::schema-body-in-interceptor
                                     :enter (fn [{{:keys [json-params]} :request :as context}]
                                              (try (s/validate schema json-params)
                                                   (catch ExceptionInfo e
                                                     (when (= (:type (ex-data e)) :schema.core/error)
                                                       (common-error/http-friendly-exception 422
                                                                                             "invalid-schema-in"
                                                                                             "The system detected that the received data is invalid"
                                                                                             (get-in (h/ex->err e) [:unknown :error])))))
                                              context)}))

(defn common-interceptors [components]
  [(body-params/body-params)
   (components-interceptor components)
   http/json-body
   error-handler-interceptor])
