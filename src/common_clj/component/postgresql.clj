(ns common-clj.component.postgresql
  (:require [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [schema.core :as s])
  (:import (org.testcontainers.containers GenericContainer PostgreSQLContainer)))

(defrecord PostgreSQL [config]
  component/Lifecycle
  (start [component]
    (let [config-content (:config config)
          postgresql-uri (-> config-content :postgresql-uri)
          connection (-> (jdbc/get-connection {:jdbcUrl postgresql-uri})
                         (jdbc/with-options {:builder-fn rs/as-unqualified-maps}))
          schema-sql (slurp "resources/schema.sql")]

      (jdbc/execute! connection [schema-sql])

      (assoc component :postgresql connection)))

  (stop [component]
    (assoc component :postgresql nil)))

(defn new-postgresql []
  (->PostgreSQL {}))

(s/defn postgresql-for-unit-tests
  [schema-sql-path :- s/Str]
  (let [postgresql-container (doto (PostgreSQLContainer. "postgres:15-alpine")
                               .start)
        connection (-> (jdbc/get-connection {:jdbcUrl (str (.getJdbcUrl postgresql-container)
                                                           "&user=test&password=test")})
                       (jdbc/with-options {:builder-fn rs/as-unqualified-maps}))]
    (jdbc/execute! connection [(slurp schema-sql-path)])
    {:database-connection  connection
     :postgresql-container postgresql-container}))

(defrecord MockPostgreSQL [config]
  component/Lifecycle
  (start [component]
    (let [postgresql-container (doto (PostgreSQLContainer. "postgres:15-alpine")
                                 .start)
          connection (-> (jdbc/get-connection {:jdbcUrl (str (.getJdbcUrl postgresql-container)
                                                             "&user=test&password=test")})
                         (jdbc/with-options {:builder-fn rs/as-unqualified-maps}))
          schema-sql (slurp "resources/schema.sql")]

      (jdbc/execute! connection [schema-sql])

      (assoc component :postgresql connection
                       :postgresql-container postgresql-container)))

  (stop [component]
    (.stop ^GenericContainer (:postgresql-container component))
    (assoc component :postgresql nil)))

(defn new-mock-postgresql []
  (->MockPostgreSQL {}))
