(ns fixtures.interceptors
  (:require [io.pedestal.interceptor :as interceptor]))

(def dumb-interceptor
  (interceptor/interceptor
    {:name  :dumb-interceptor
     :enter (fn [_] nil)}))
