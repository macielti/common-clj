(ns common-clj.io.interceptors
  (:require [io.pedestal.http.body-params :as body-params]
            [io.pedestal.interceptor.error :as error]
            [clojure.tools.logging :as log]
            [io.pedestal.http :as http]))

(def error-handler-interceptor
  (error/error-dispatch [ctx ex]
                        [{:exception-type :clojure.lang.ExceptionInfo}]
                        (let [{:keys [status cause reason]} (ex-data ex)]
                          (assoc ctx :response {:status status
                                                :body   {:cause (or cause reason)}}))

                        :else
                        (do (log/error ex)
                            (assoc ctx :response {:status 500 :body nil}))))

(def common-interceptors [(body-params/body-params)
                          http/json-body
                          error-handler-interceptor])
