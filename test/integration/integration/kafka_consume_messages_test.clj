(ns integration.kafka-consume-messages-test
  (:require [clojure.test :refer :all]
            [schema.test :as s-test]
            [cheshire.core :as json]
            [schema.core :as s]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.kafka.consumer :as component.consumer]
            [common-clj.component.helper.core :as component.helper])
  (:import (org.apache.kafka.clients.consumer ConsumerRecord)))

(def test-state (atom nil))

(defn ^:private test-topic-consumer
  [message
   components]
  (reset! test-state message))

(s/defschema ^:private TestMessage
  {:topic (s/enum :consumer-topic-test)
   :value {:test-title s/Str}})

(def ^:private topic-consumers
  {:consumer-topic-test {:schema  TestMessage
                         :handler test-topic-consumer}})

(def ^:private system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test)
    :consumer (component/using (component.consumer/new-consumer topic-consumers) [:config])))

(s-test/deftest kafka-consumer-component-test
  (let [system   (component/start system-test)
        consumer (component.helper/get-component-content :consumer system)]


    (.addRecord (:kafka-client consumer)
                (ConsumerRecord. "consumer-topic-test" 0 (long 0) nil (json/encode {:test-title "just a simple test"})))

    (Thread/sleep 5000)

    (testing "that we can use kafka consumer to consumer messages"
      (is (= {:topic :consumer-topic-test
              :value {:test-title "just a simple test"}}
             @test-state)))

    (reset! test-state nil)
    (component/stop system)))
