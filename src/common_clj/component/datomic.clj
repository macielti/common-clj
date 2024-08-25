(ns common-clj.component.datomic
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d]
            [datomic.client.api :as dl]
            [taoensso.timbre :as log]))

(defn mocked-datomic [datomic-schemas]
  (let [datomic-uri "datomic:mem://unit-tests"
        connection (do (d/create-database datomic-uri)
                       (d/connect datomic-uri))]
    @(d/transact connection (flatten datomic-schemas))
    connection))

(defrecord Datomic [config schemas]
  component/Lifecycle
  (start [component]
    (let [datomic-uri (or (-> config :config :datomic-uri)
                          "datomic:mem://integration-tests")
          connection (do (log/info ::database-creation (d/create-database datomic-uri))
                         (d/connect datomic-uri))]
      @(d/transact connection (flatten schemas))
      (assoc component :datomic connection)))

  (stop [{:keys [datomic] :as component}]
    (d/release datomic)
    (assoc component :datomic nil)))

(defn new-datomic [schemas]
  (map->Datomic {:schemas schemas}))

(defn mocked-datomic-local [datomic-schemas]
  (let [db-name (str (random-uuid))
        client (dl/client {:server-type :datomic-local
                           :storage-dir (str "/tmp/datomic-local-test/" db-name)
                           :system      "test"})
        _ (dl/create-database client {:db-name db-name})
        connection (dl/connect client {:db-name db-name})]
    (dl/transact connection {:tx-data (flatten datomic-schemas)})
    connection))

(defrecord DatomicLocal [config schemas]
  component/Lifecycle
  (start [component]
    (let [config' (:config config)
          current-env (:current-env config')
          storage-dir (-> (:datomic-local config') :storage-dir)
          db-name (case current-env
                    :prod (-> (:datomic-local config') :db-name)
                    :test (str (random-uuid)))
          client (dl/client {:server-type :datomic-local
                             :storage-dir storage-dir
                             :system      "prod"})
          _ (dl/create-database client {:db-name db-name})
          connection (dl/connect client {:db-name db-name})]

      (dl/transact connection {:tx-data (flatten schemas)})

      (assoc component :datomic {:connection connection})))

  (stop [component]
    component))

(defn new-datomic-local [schemas]
  (map->DatomicLocal {:schemas schemas}))
