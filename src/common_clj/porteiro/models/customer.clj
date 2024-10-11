(ns common-clj.porteiro.models.customer
  (:require [schema.core :as s]))

(def base-customer
  {:customer/id                     s/Uuid
   :customer/username               s/Str
   (s/optional-key :customer/name)  s/Str
   (s/optional-key :customer/roles) [s/Keyword]
   :customer/hashed-password        s/Str})

(s/defschema Customer
  base-customer)

(s/defschema CustomerAuthentication
  "Schema for customer authentication"
  {:username s/Str
   :password s/Str})
