(ns common-clj.component.kafka.consumer-test
  (:require [clojure.test :refer :all]
            [common-clj.component.kafka.consumer :as component.kafka.consumer]
            [matcher-combinators.test :refer [match?]]
            [cheshire.core :as json])
  (:import (org.apache.kafka.clients.producer ProducerRecord)))

(def kafka-message-record (ProducerRecord. (name :test-topic) (json/encode {:payload {:test-key :test-value}})))

(deftest kafka-record->clj-message-test
  (testing "that we can convert a kafka record to a message map"
    (is (match? {:topic :test-topic
                 :data  {:payload {:test-key "test-value"}}}
                (component.kafka.consumer/kafka-record->clj-message kafka-message-record)))))
