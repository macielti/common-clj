(ns common-clj.io.interceptors.customer
  (:require [buddy.sign.jwt :as jwt]
            [camel-snake-kebab.core :as camel-snake-kebab]
            [clojure.string :as str]
            [common-clj.error.core :as common-error]
            [schema.core :as s])
  (:import (clojure.lang ExceptionInfo)
           (java.util UUID)))

(s/defschema CustomerIdentity
  {:customer/id    s/Uuid
   :customer/roles [s/Keyword]})

(s/defn ^:private wire-jwt->customer-identity :- CustomerIdentity
  [jwt-wire :- s/Str
   jwt-secret :- s/Str]
  (try (let [{:keys [id roles]} (:customer (jwt/unsign jwt-wire jwt-secret))]
         {:customer/id    (UUID/fromString id)
          :customer/roles (map camel-snake-kebab/->kebab-case-keyword roles)})
       (catch ExceptionInfo _ (common-error/http-friendly-exception 422
                                                                    "invalid-jwt"
                                                                    "Invalid JWT"
                                                                    "Invalid JWT"))))

(def identity-interceptor
  {:name  ::identity-interceptor
   :enter (fn [{{{:keys [config]} :components
                 headers          :headers} :request :as context}]
            (assoc-in context [:request :customer]
                      (wire-jwt->customer-identity (-> (get headers "authorization")
                                                       (str/split #" ")
                                                       last) (:jwt-secret config))))})

(s/defn required-roles-interceptor
  [required-roles :- [s/Keyword]]
  {:name  ::required-roles-interceptor
   :enter (fn [{{{customer-roles :customer/roles} :customer} :request :as context}]
            (if (empty? (clojure.set/difference (set required-roles) (set customer-roles)))
              context
              (common-error/http-friendly-exception 403
                                                    "insufficient-roles"
                                                    "Insufficient privileges/roles/permission"
                                                    "Insufficient privileges/roles/permission")))})
