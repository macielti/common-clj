(ns common-clj.integrant-components.postgresql-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.integrant-components.postgresql :as postgresql]
            [java-time.api :as jt]
            [pg.core :as pg]
            [schema.test :as s]))

(s/deftest mocked-postgresql-conn-test
  (testing "That we can connect to a mocked postgresql container"
    (let [conn (postgresql/mocked-postgresql-conn)
          now (jt/local-date)]
      (is (= [{:nome       "nascimento"
               :apelido    "brunão"
               :nascimento now}]
             (pg/execute conn
                         "INSERT INTO pessoa (apelido, nome, nascimento) VALUES ($1, $2, $3)
                          returning *"
                         {:params ["brunão" "nascimento" now]}))))))
