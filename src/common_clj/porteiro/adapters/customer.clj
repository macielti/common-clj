(ns common-clj.porteiro.adapters.customer
  (:require [camel-snake-kebab.core :as camel-snake-kebab]
            [common-clj.porteiro.models.customer :as models.customer]
            [medley.core :as medley]
            [schema.core :as s]
            [buddy.hashers :as hashers]
            [common-clj.porteiro.wire.in.customer :as wire.in.customer]
            [common-clj.porteiro.wire.out.customer :as wire.out.customer]))

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
                      :roles    (map camel-snake-kebab/->SCREAMING_SNAKE_CASE_STRING roles)}
                     :name name))

(s/defn wire->internal-customer-authentication :- models.customer/CustomerAuthentication
  [{:keys [username password]} :- wire.in.customer/CustomerAuthentication]
  {:username username
   :password password})

(s/defn customer-token->wire :- wire.out.customer/CustomerToken
  [token :- s/Str]
  {:token token})
