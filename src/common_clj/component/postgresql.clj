(ns common-clj.component.postgresql
  (:require [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]
            [schema.core :as s]
            [jdbc.pool.c3p0 :as pool])
  (:import (org.testcontainers.containers GenericContainer PostgreSQLContainer)))

(defrecord PostgreSQL [config]
  component/Lifecycle
  (start [component]
    (let [{{:keys [username password host port database]} :postgresql} (:config config)
          db-connection (pool/make-datasource-spec
                          {:classname         "org.postgresql.Driver"
                           :subprotocol       "postgresql"
                           :user              username
                           :password          password
                           :subname           (str "//" host ":" port "/" database)
                           :initial-pool-size 3
                           :max-pool-size     14})
          schema-sql (slurp "resources/schema.sql")]

      (jdbc/execute! db-connection [schema-sql])

      (assoc component :postgresql db-connection)))

  (stop [component]
    (assoc component :postgresql nil)))

(defn new-postgresql []
  (->PostgreSQL {}))

(s/defn connection-pool-for-unit-tests
  [schema-sql-path :- s/Str]
  (let [postgresql-container (doto (PostgreSQLContainer. "postgres:16-alpine")
                               .start)
        db-connection (pool/make-datasource-spec
                        {:classname         "org.postgresql.Driver"
                         :subprotocol       "postgresql"
                         :user              (.getUsername postgresql-container)
                         :password          (.getPassword postgresql-container)
                         :subname           (str "//" (.getHost postgresql-container)
                                                 ":" (.getMappedPort postgresql-container
                                                                     PostgreSQLContainer/POSTGRESQL_PORT)
                                                 "/" (.getDatabaseName postgresql-container))
                         :initial-pool-size 3
                         :max-pool-size     14})]

    (jdbc/execute! db-connection [(slurp schema-sql-path)])

    {:database-connection  db-connection
     :postgresql-container postgresql-container}))

(defrecord MockPostgreSQL [config]
  component/Lifecycle
  (start [component]
    (let [postgresql-container (doto (PostgreSQLContainer. "postgres:16-alpine")
                                 .start)
          db-connection (pool/make-datasource-spec
                          {:classname         "org.postgresql.Driver"
                           :subprotocol       "postgresql"
                           :user              (.getUsername postgresql-container)
                           :password          (.getPassword postgresql-container)
                           :subname           (str "//" (.getHost postgresql-container)
                                                   ":" (.getMappedPort postgresql-container
                                                                       PostgreSQLContainer/POSTGRESQL_PORT)
                                                   "/" (.getDatabaseName postgresql-container))
                           :initial-pool-size 3
                           :max-pool-size     14})
          schema-sql (slurp "resources/schema.sql")]

      (jdbc/execute! db-connection [schema-sql])

      (assoc component :postgresql db-connection
                       :postgresql-container postgresql-container)))

  (stop [component]
    (.stop ^GenericContainer (:postgresql-container component))
    (assoc component :postgresql nil)))

(defn new-mock-postgresql []
  (->MockPostgreSQL {}))
