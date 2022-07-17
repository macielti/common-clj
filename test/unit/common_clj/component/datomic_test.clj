(ns common-clj.component.datomic-test
  (:require [clojure.test :refer :all]
            [common-clj.component.datomic :as component.datomic]
            [datomic.api :as d]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as s])
  (:import (java.util UUID)))

(def schemas
  [{:db/ident       :test/id
    :db/valueType   :db.type/uuid
    :db/unique      :db.unique/identity
    :db/cardinality :db.cardinality/one
    :db/doc         "Test Id"}
   {:db/ident       :test/value
    :db/valueType   :db.type/keyword
    :db/unique      :db.unique/identity
    :db/cardinality :db.cardinality/one
    :db/doc         "Test value"}])

(s/deftest mocked-datomic-test
  (let [mocked-datomic (component.datomic/mocked-datomic schemas)]
    (testing "that we can insert entities"
      (d/transact mocked-datomic [{:test/id    (UUID/randomUUID)
                                   :test/value :insert/query}]))
    (testing "that we can query a inserted entity"
      (is (match? [[{:db/id      int?
                     :test/id    uuid?
                     :test/value :insert/query}]]
                  (d/q '[:find (pull ?test [*])
                         :in $
                         :where
                         [?test :test/value :insert/query]] (d/db mocked-datomic)))))))
