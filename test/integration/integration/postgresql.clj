(ns integration.postgresql
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.postgres-jdbc :as component.postgres-jdbc]
            [next.jdbc :as jdbc]
            [clojure.instant :as instant]
            [schema.test :as s]))

(def system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.edn" :test :edn)
    :postgresql (component/using (component.postgres-jdbc/new-postgreslq) [:config])))

(s/deftest postresql-component-test
  (let [system (component/start system-test)
        postgresql (component.helper/get-component-content :postgresql system)]
    (jdbc/execute! postgresql ["INSERT INTO pessoa (apelido, nome, nascimento) VALUES (?, ?, ?)" "brunão" "nascimento" (instant/read-instant-timestamp "2000-10-01")])
    (is (= [{:apelido    "brunão"
             :nascimento #inst "2000-09-30T03:00:00.000-00:00"
             :nome       "nascimento"}]
           (jdbc/execute! postgresql ["SELECT apelido, nascimento, nome FROM pessoa WHERE nome = ?" "nascimento"])))
    (jdbc/execute! postgresql ["DROP TABLE pessoa"])
    (component/stop system)))