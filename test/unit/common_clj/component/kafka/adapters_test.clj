(ns common-clj.component.kafka.adapters-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [is testing]]
            [common-clj.component.kafka.adapters :as component.kafka.adapters]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as s])
  (:import (org.apache.kafka.clients.producer ProducerRecord)))

(def kafka-message-record (ProducerRecord. (name :test-topic) (json/encode {:payload {:test-key :test-value}})))

(s/deftest kafka-record->clj-message-test
  (testing "that we can convert a kafka record to a message map"
    (is (match? {:topic :test-topic
                 :data  {:payload {:test-key "test-value"}}}
                (component.kafka.adapters/kafka-record->clj-message kafka-message-record)))))
