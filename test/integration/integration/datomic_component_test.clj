(ns integration.datomic-component-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.datomic :as component.datomic]
            [clojure.test.check.generators :as generators]
            [schema-refined.core :as r]
            [datomic.api :as d]
            [schema.core :as s]
            [schema-generators.generators :as g]
            [common-clj.component.helper.core :as component.helper]))

(def ^:private user-skeleton
  [{:db/ident       :user/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "User Id"}
   {:db/ident       :user/username
    :db/valueType   :db.type/string
    :db/unique      :db.unique/identity
    :db/cardinality :db.cardinality/one
    :db/doc         "User username"}])

(def ^:private schemas [user-skeleton])

(s/defschema ^:private User
  #:user{:id       s/Uuid
         :username (r/refined s/Str r/NonEmptyStr)})

(def ^:private user-test
  (g/generate User {s/Str generators/string-alphanumeric}))

(def ^:private user-test-2
  (g/generate User {s/Str generators/string-alphanumeric}))

(defn ^:private insert-an-user!
  [user connection]
  (d/transact connection [user]))

(defn ^:private query-user-by-id
  [user-id connection]
  (-> (d/q '[:find (pull ?e [:user/id :user/username])
             :in $ ?user-id
             :where [?e :user/id ?user-id]] (d/db connection) user-id)
      ffirst))

(def ^:private system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test :json)
    :datomic (component/using (component.datomic/new-datomic schemas)
                              [:config])))

(deftest datomic-component-test
  (let [system     (component/start system-test)
        connection (:connection (component.helper/get-component-content :datomic system))]

    (testing "that we can start the datomic component completely"
      (is (true? (boolean connection)))

      (testing "that the schemas were transacted"
        @(insert-an-user! user-test connection)
        (is (thrown? Exception @(insert-an-user! (assoc user-test-2 :user/email "example@example.com") connection))))

      (testing "that can query data from the datomic database"
        (is (= user-test
               (query-user-by-id (:user/id user-test) connection)))))

    (testing "that we can stop the datomic component completely"
      (let [system-after-stop (component/stop-system system)]

        (testing "that the stopped component exists"
          (is (true? (-> (get-in system-after-stop [:datomic])
                         (contains? :datomic)))))

        (testing "that the component was stopped"
          (is (false? (boolean (component.helper/get-component-content :datomic system-after-stop)))))

        (testing "that we can't transact using a stopped datomic component"
          (is (thrown? Exception  (query-user-by-id (:user/id user-test) connection))))))))
