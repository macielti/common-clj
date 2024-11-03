(ns common-clj.porteiro.db.postgresql.customer-test
  (:require [clojure.test :refer :all]
            [common-clj.porteiro.db.postgresql.customer :as database.customer]
            [common-clj.porteiro.models.customer :as models.customer]
            [common-clj.test.helper.schema :as test.helper.schema]
            [common-test-clj.component.postgresql-mock :as component.postgresql-mock]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as s]))

(def customer-id (random-uuid))
(def customer
  (test.helper.schema/generate models.customer/Customer
                               {:customer/id       customer-id
                                :customer/username "magal"}))

(s/deftest insert-test
  (testing "Should insert a customer"
    (let [conn (component.postgresql-mock/postgresql-conn-mock)]
      (is (match? {:customer/hashed-password string?
                   :customer/id              uuid?
                   :customer/roles           ()
                   :customer/username        string?}
                  (database.customer/insert! customer conn))))))

(s/deftest by-username-test
  (testing "Should be able to query a customer by username"
    (let [conn (component.postgresql-mock/postgresql-conn-mock)]
      (database.customer/insert! customer conn)
      (is (match? {:customer/id              uuid?
                   :customer/username        "magal"
                   :customer/hashed-password string?
                   :customer/roles           []}
                  (database.customer/by-username "magal" conn)))

      (is (nil? (database.customer/by-username "random-username" conn))))))

(s/deftest lookup-test
  (testing "Should be able to query a customer by id"
    (let [conn (component.postgresql-mock/postgresql-conn-mock)]
      (database.customer/insert! customer conn)
      (is (match? {:customer/id              uuid?
                   :customer/username        "magal"
                   :customer/hashed-password string?
                   :customer/roles           []}
                  (database.customer/lookup customer-id conn)))

      (is (nil? (database.customer/lookup (random-uuid) conn))))))

(s/deftest add-role-test
  (testing "Should be able to query a customer by id"
    (let [conn (component.postgresql-mock/postgresql-conn-mock)]
      (database.customer/insert! customer conn)
      (is (match? {:customer/id              uuid?
                   :customer/username        "magal"
                   :customer/hashed-password string?
                   :customer/roles           [:test]}
                  (database.customer/add-role! customer-id :test conn)))

      (is (nil? (database.customer/lookup (random-uuid) conn))))))
