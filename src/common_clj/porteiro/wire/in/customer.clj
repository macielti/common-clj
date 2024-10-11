(ns common-clj.porteiro.wire.in.customer
  (:require [schema.core :as s]))

(s/defschema Customer
  "Schema for customer creation request"
  {:username s/Str
   :password s/Str})

(s/defschema CustomerCreationDocument
  "Schema for customer creation request"
  {:customer Customer})

(s/defschema CustomerAuthentication
  "Schema for customer authentication request"
  {:username s/Str
   :password s/Str})

(s/defschema CustomerAuthenticationDocument
  "Schema for customer authentication document request"
  {:customer CustomerAuthentication})
