(ns common-clj.io.interceptors.postgresql
  (:require [common-clj.error.core :as common-error]
            [io.pedestal.interceptor :as pedestal.interceptor]
            [pg.core :as pg]
            [pg.pool :as pool]
            [schema.core :as s]))

(s/defn resource-existence-check-interceptor
  "resource-identifier-fn -> function used to extract param used to query the resource, must receive a context as argument.
  sql-query -> postgresql query that will try to find the resource using the resource identifier"
  [resource-identifier-fn
   sql-query]
  (pedestal.interceptor/interceptor {:name  ::resource-existence-check-interceptor
                                     :enter (fn [{{:keys [components]} :request :as context}]
                                              (let [pool (:postgresql components)
                                                    resource-identifier (resource-identifier-fn context)
                                                    resource (-> (pool/with-connection [database-conn pool]
                                                                   (pg/execute database-conn sql-query {:params [resource-identifier]})) first)]
                                                (when-not resource
                                                  (common-error/http-friendly-exception 404
                                                                                        "resource-not-found"
                                                                                        "Resource could not be found"
                                                                                        "Not Found")))
                                              context)}))
