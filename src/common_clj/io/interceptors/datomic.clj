(ns common-clj.io.interceptors.datomic
  (:require [common-clj.error.core :as common-error]
            [datomic.api :as d]
            [io.pedestal.interceptor :as pedestal.interceptor]
            [schema.core :as s]))

(s/defn resource-existence-check-interceptor
  "resource-identifier-fn -> function used to extract param used to query the resource, must receive a context as argument.
  datomic-query -> datomic query that will try to find the resource using the resource identifier"
  [resource-identifier-fn
   datomic-query]
  (pedestal.interceptor/interceptor {:name  ::resource-existence-check-interceptor
                                     :enter (fn [{{:keys [components]} :request :as context}]
                                              (let [database-conn (:datomic components)
                                                    resource-identifier (resource-identifier-fn context)
                                                    resource (-> (d/q datomic-query (d/db database-conn) resource-identifier) ffirst (dissoc :db/id))]
                                                (when-not resource
                                                  (common-error/http-friendly-exception 404
                                                                                        "resource-not-found"
                                                                                        "Resource could not be found"
                                                                                        "Not Found")))
                                              context)}))
