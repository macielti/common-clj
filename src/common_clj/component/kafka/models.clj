(ns common-clj.component.kafka.models
  (:require [schema.core :as s]))

(s/defschema KafkaMessage
  {:topic s/Keyword
   :data  {:payload        {s/Keyword (s/maybe s/Any)}
           (s/optional-key :meta) {(s/optional-key :correlation-id) s/Str}}})
