(ns integration.postgresql
  (:require [clojure.instant :as instant]
            [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.postgresql :as component.postgresql]
            [common-clj.test.helper.components.containers :as test.helper.components.containers]
            [matcher-combinators.test :refer [match?]]
            [next.jdbc :as jdbc]
            [schema.test :as s]))

(def system-test
  (component/system-map
   :config (component.config/new-config "resources/config_test.edn" :test :edn)
   :containers (test.helper.components.containers/new-containers #{:postgresql})
   :postgresql (component/using (component.postgresql/new-mock-postgresql) [:config :containers])))

(s/deftest postresql-component-test
  (let [system (component/start system-test)
        postgresql-pool (component.helper/get-component-content :postgresql system)]
    (jdbc/execute! postgresql-pool ["INSERT INTO pessoa (apelido, nome, nascimento)
                                     VALUES (?, ?, ?)"
                                    "brunão" "nascimento" (instant/read-instant-timestamp "2020-03-23T01:17Z")])
    (is (match? [{:pessoa/apelido    "brunão"
                  :pessoa/nascimento inst?
                  :pessoa/nome       "nascimento"}]
                (jdbc/execute! postgresql-pool ["SELECT apelido, nascimento, nome
                                                 FROM pessoa
                                                 WHERE nome = ?" "nascimento"])))
    (component/stop system)))
