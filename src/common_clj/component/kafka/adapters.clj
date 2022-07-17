(ns common-clj.component.kafka.adapters
  (:require [cheshire.core :as json]
            [common-clj.component.kafka.models :as component.kafka.models]
            [schema.core :as s]))

(s/defn kafka-record->clj-message :- component.kafka.models/KafkaMessage
  [record]
  (let [message (json/decode (.value record) true)]
    {:topic (keyword (.topic record))
     :data  {:payload (:payload message)}}))
