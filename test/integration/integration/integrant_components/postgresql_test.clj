(ns integration.integrant-components.postgresql-test
  (:require [clojure.instant :as instant]
            [clojure.test :refer :all]
            [common-clj.integrant-components.postgresql :as postgresql]
            [integrant.core :as ig]
            [java-time.api :as jt]
            [pg.core :as pg]
            [schema.test :as s]))

(def postgresql-mock-config
  {::postgresql/postgresql-mock {}})


(s/deftest postgresql-mock-config-test
  (testing "That we can define endpoints"
    (let [system (ig/init postgresql-mock-config)]
      #p (-> system
             :common-clj.integrant-components.postgresql/postgresql-mock
             (pg/execute "INSERT INTO pessoa (apelido, nome, nascimento) VALUES ($1, $2, $3)"
                         {:params ["brunão" "nascimento" (jt/local-date-time)]}))
      (ig/halt! system))))
