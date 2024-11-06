(ns integration.aux.http
  (:require [cheshire.core :as json]
            [io.pedestal.test :as test]))

(defn get-request-test-endpoints
  [path
   headers
   service-fn]
  (let [{:keys [body status]} (test/response-for service-fn
                                                 :get path
                                                 :headers headers)]
    {:status status
     :body   (json/decode body true)}))

(defn post-request-to-test-endpoint
  [path
   body
   service-fn]
  (let [{:keys [body status]} (test/response-for service-fn
                                                 :post path
                                                 :headers {"Content-Type" "application/json"}
                                                 :body (json/encode body))]
    {:status status
     :body   (json/decode body true)}))
