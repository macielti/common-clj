(ns common-clj.component.telegram.adapters.update-test
  (:require [clojure.test :refer :all]
            [schema.test :as s]
            [fixtures.update]
            [common-clj.component.telegram.adapters.update :as component.telegram.adapters.update]))

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
