(ns common-clj.integrant-components.config-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.integrant-components.config :as component.config]
            [schema.test :as s]))

(s/deftest config-file-test
  (testing "That we can read a config file"
    (is (= {:prod {:bootstrap-server       "http://localhost:9092"
                   :service-authentication {:auth-server-base-url "https://example.com"
                                            :password             "random-password"
                                            :username             "service-name"}}
            :test {:bootstrap-server                              "http://localhost:9092"
                   :dead-letter-queue-service-integration-enabled true
                   :new-relic-api-key                             "random-api-key"
                   :service                                       {:host "0.0.0.0"
                                                                   :port 8000}
                   :service-authentication                        {:auth-server-base-url "https://example.com"
                                                                   :password             "random-password"
                                                                   :username             "service-name"}
                   :service-name                                  "test-service-name"
                   :topics                                        ["test.example"]}}
           (component.config/config-file! "test/resources/config_test.edn")))))
