(ns integration.aux.http
  (:require [io.pedestal.test :as test]
            [clojure.test :refer :all]
            [cheshire.core :as json]))

(defn request-test-endpoints
  [path
   service-fn]
  (let [{:keys [body status]} (test/response-for service-fn :get path)]
    {:status status
     :body   (json/decode body true)}))
