(ns integration.integrant-components.config-component-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.integrant-components.config]
            [integrant.core :as ig]
            [schema.test :as s]))

(def config-prod
  {:common-clj.integrant-components.config/config {:path "test/resources/config_test.edn"
                                                   :env  :prod}})

(def config-test
  {:common-clj.integrant-components.config/config {:path "test/resources/config_test.edn"
                                                   :env  :test}})

(def config-prod-override-props
  {:common-clj.integrant-components.config/config {:path      "test/resources/config_test.edn"
                                                   :env       :prod
                                                   :overrides {:bootstrap-server "http://127.0.0.1:8080"}}})

(s/deftest config-prod-component-override-props-test
  (testing "That we can define endpoints"
    (let [system (ig/init config-prod-override-props)]
      (is (= {:bootstrap-server       "http://127.0.0.1:8080"
              :current-env            :prod
              :service-authentication {:auth-server-base-url "https://example.com"
                                       :password             "random-password"
                                       :username             "service-name"}}
             (:common-clj.integrant-components.config/config system)))
      (ig/halt! system))))

(s/deftest config-prod-component-test
  (testing "That we can define endpoints"
    (let [system (ig/init config-prod)]
      (is (= {:bootstrap-server       "http://localhost:9092"
              :current-env            :prod
              :service-authentication {:auth-server-base-url "https://example.com"
                                       :password             "random-password"
                                       :username             "service-name"}}
             (:common-clj.integrant-components.config/config system)))
      (ig/halt! system))))

(s/deftest config-test-component-test
  (testing "That we can define endpoints"
    (let [system (ig/init config-test)]
      (is (= {:bootstrap-server                              "http://localhost:9092"
              :current-env                                   :test
              :dead-letter-queue-service-integration-enabled true
              :new-relic-api-key                             "random-api-key"
              :service                                       {:host "0.0.0.0"
                                                              :port 8000}
              :service-authentication                        {:auth-server-base-url "https://example.com"
                                                              :password             "random-password"
                                                              :username             "service-name"}
              :service-name                                  "test-service-name"
              :topics                                        ["test.example"]}
             (:common-clj.integrant-components.config/config system)))
      (ig/halt! system))))
