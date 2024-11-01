(ns common-clj.integrant-components.postgresql
  (:require [integrant.core :as ig]
            [pg.core :as pg]
            [pg.migration.core :as mig]
            [taoensso.timbre :as log])
  (:import (org.testcontainers.containers PostgreSQLContainer)))

(defmethod ig/init-key ::postgresql
  [_ {:keys [components]}]
  (log/info :starting ::postgresql)
  (let [postgresql-config (-> components :config :postgresql)
        connection (pg/connect postgresql-config)]
    (mig/migrate-all postgresql-config)
    connection))

(defmethod ig/halt-key! ::postgresql
  [_ connection]
  (log/info :stopping ::postgresql)
  (pg/close connection))

(defmethod ig/init-key ::postgresql-mock
  [_ _]
  (log/info :starting ::postgresql-mock)
  (let [postgresql-container (doto (PostgreSQLContainer. "postgres:16-alpine") .start)
        postgresql-config {:host     (.getHost postgresql-container)
                           :port     (.getMappedPort postgresql-container PostgreSQLContainer/POSTGRESQL_PORT)
                           :user     (.getUsername postgresql-container)
                           :password (.getPassword postgresql-container)
                           :database (.getDatabaseName postgresql-container)}
        connection (pg/connect postgresql-config)]
    (mig/migrate-all postgresql-config)
    connection))

(defmethod ig/halt-key! ::postgresql-mock
  [_ connection]
  (log/info :stopping ::postgresql-mock)
  (pg/close connection))

