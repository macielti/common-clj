(ns common-clj.component.postgresql
  (:require [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(defrecord PostgreSQL [config]
  component/Lifecycle
  (start [component]
    (let [postgresql-uri (-> config :config :postgresql-uri)
          connection (-> (jdbc/get-connection {:jdbcUrl postgresql-uri})
                         (jdbc/with-options {:builder-fn rs/as-unqualified-maps}))
          schema-sql (slurp "resources/schema.sql")]

      (jdbc/execute! connection [schema-sql])

      (assoc component :postgresql connection)))

  (stop [component]
    (assoc component :postgresql nil)))

(defn new-postgreslq []
  (map->PostgreSQL {}))