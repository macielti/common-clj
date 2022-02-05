(ns common-clj.io.interceptors
  (:require [io.pedestal.http.body-params :as body-params]
            [io.pedestal.interceptor.error :as error]
            [io.pedestal.interceptor :as pedestal.interceptor]
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
                            (assoc ctx :response {:status 500 :body {:cause "Internal Server Error"}}))))

(defn components-interceptor [system-components]
  (pedestal.interceptor/interceptor {:name  ::components-interceptor
                                     :enter (fn [context]
                                              (assoc-in context [:request :components] system-components))}))

(defn common-interceptors [components]
  [(body-params/body-params)
   (components-interceptor components)
   http/json-body
   error-handler-interceptor])
