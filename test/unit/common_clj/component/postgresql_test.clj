(ns common-clj.component.postgresql-test
  (:require [clojure.instant :as instant]
            [clojure.test :refer :all]
            [next.jdbc :as jdbc]
            [schema.test :as s]
            [common-clj.component.postgresql :as component.postgresql]
            [matcher-combinators.test :refer [match?]]))

(s/deftest postgresql-for-unit-tests-test
  (let [postgresql-pool (component.postgresql/connection-pool-for-unit-tests "resources/schema.sql")]
    (jdbc/execute! postgresql-pool ["INSERT INTO pessoa (apelido, nome, nascimento)
                                     VALUES (?, ?, ?)"
                                    "brunão" "nascimento" (instant/read-instant-timestamp "2020-03-23T01:17Z")])
    (is (match? [{:pessoa/apelido    "brunão"
                  :pessoa/nascimento inst?
                  :pessoa/nome       "nascimento"}]
                (jdbc/execute! postgresql-pool ["SELECT apelido, nascimento, nome
                                                 FROM pessoa
                                                 WHERE nome = ?" "nascimento"])))))
