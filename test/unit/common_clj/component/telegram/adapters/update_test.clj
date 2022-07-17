(ns common-clj.component.telegram.adapters.update-test
  (:require [clojure.test :refer :all]
            [common-clj.component.telegram.adapters.update :as component.telegram.adapters.update]
            [fixtures.update]
            [schema.test :as s]))

(s/deftest update->consumer-key-test
  (testing "that we can extract the consumer key from a update entity from telegram bot"
    (is (= :test
           (component.telegram.adapters.update/update->consumer-key
            fixtures.update/update-with-test-command-call
            :message)))
    (is (= :callback-query
           (component.telegram.adapters.update/update->consumer-key
            fixtures.update/update-with-callback-query
            :callback-query)))))
