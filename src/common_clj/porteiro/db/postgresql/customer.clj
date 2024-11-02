(ns common-clj.porteiro.db.postgresql.customer
  (:require [common-clj.porteiro.adapters.customer :as adapters.customer]
            [common-clj.porteiro.models.customer :as models.customer]
            [pg.core :as pg]
            [schema.core :as s]))

(s/defn insert! :- models.customer/Customer
  [{:customer/keys [id username name roles hashed-password]} :- models.customer/Customer
   database-conn]
  (-> (pg/execute database-conn
                  "INSERT INTO customers (id, username, name, roles, hashed_password) VALUES ($1, $2, $3, $4, $5)
                  returning *"
                  {:params [id username name (or roles []) hashed-password]})
      first
      adapters.customer/postgresql->internal))

(s/defn by-username :- (s/maybe models.customer/Customer)
  [username :- s/Str
   database-conn]
  (some-> (pg/execute database-conn
                      "SELECT * FROM customers WHERE username = $1"
                      {:params [username]})
          first
          adapters.customer/postgresql->internal))

(s/defn lookup :- (s/maybe models.customer/Customer)
  [customer-id :- s/Uuid
   database-conn]
  (some-> (pg/execute database-conn
                      "SELECT * FROM customers WHERE id = $1"
                      {:params [customer-id]})
          first
          adapters.customer/postgresql->internal))

(s/defn add-role! :- (s/maybe models.customer/Customer)
  [customer-id :- s/Uuid
   role :- s/Keyword
   database-conn]
  (some-> (pg/execute database-conn
                      "UPDATE customers SET roles = array_append(roles, $1) WHERE id = $2 returning *"
                      {:params [(adapters.customer/internal-role->wire-role role) customer-id]})
          first
          adapters.customer/postgresql->internal))
