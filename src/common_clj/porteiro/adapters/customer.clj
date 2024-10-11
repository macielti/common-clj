(ns common-clj.porteiro.adapters.customer
  (:require [buddy.hashers :as hashers]
            [camel-snake-kebab.core :as camel-snake-kebab]
            [common-clj.porteiro.models.customer :as models.customer]
            [common-clj.porteiro.wire.in.customer :as wire.in.customer]
            [common-clj.porteiro.wire.out.customer :as wire.out.customer]
            [medley.core :as medley]
            [schema.core :as s]))

(s/defn wire->internal :- models.customer/Customer
  [{:keys [username password name]} :- wire.in.customer/Customer]
  (medley/assoc-some {:customer/id              (random-uuid)
                      :customer/username        username
                      :customer/hashed-password (hashers/derive password)}
                     :customer/name name))

(s/defn internal->wire :- wire.out.customer/Customer
  [{:customer/keys [id username roles name] :or {roles []}} :- models.customer/Customer]
  (medley/assoc-some {:id       (str id)
                      :username username
                      :roles    (map clojure.core/name roles)}
                     :name name))

(s/defn wire->internal-customer-authentication :- models.customer/CustomerAuthentication
  [{:keys [username password]} :- wire.in.customer/CustomerAuthentication]
  {:username username
   :password password})

(s/defn customer-token->wire :- wire.out.customer/CustomerToken
  [token :- s/Str]
  {:token token})

(s/defn wire->internal-role :- s/Keyword
  [wire-role :- s/Str]
  (camel-snake-kebab/->kebab-case-keyword wire-role))
