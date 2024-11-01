(ns integration.integrant-components.postgresql-test
  (:require [clojure.test :refer :all]
            [common-clj.integrant-components.postgresql :as postgresql]
            [integrant.core :as ig]
            [schema.test :as s]))

(def postgresql-mock-config
  {::postgresql/postgresql-mock {}})


(s/deftest postgresql-mock-config-test
  (testing "That we can define endpoints"
    (let [system (ig/init postgresql-mock-config)]
      #p system
      (ig/halt! system))))
