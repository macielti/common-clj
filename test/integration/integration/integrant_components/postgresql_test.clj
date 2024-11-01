(ns integration.integrant-components.postgresql-test
  (:require [clojure.test :refer :all]
            [common-clj.integrant-components.postgresql :as postgresql]
            [integrant.core :as ig]
            [java-time.api :as jt]
            [pg.core :as pg]
            [pg.pool :as pool]
            [schema.test :as s]))

(def postgresql-mock-config
  {::postgresql/postgresql-mock {}})

(s/deftest postgresql-mock-config-test
  (testing "That we can define endpoints"
    (let [system (ig/init postgresql-mock-config)
          now (jt/local-date)]
      (is (= [{:apelido    "brunão"
               :nascimento now
               :nome       "nascimento"}]
             (pool/with-connection
               [conn (:common-clj.integrant-components.postgresql/postgresql-mock system)]
               (pg/execute conn
                           "INSERT INTO pessoa (apelido, nome, nascimento) VALUES ($1, $2, $3)
                            returning *"
                           {:params ["brunão" "nascimento" now]}))))
      (ig/halt! system))))
