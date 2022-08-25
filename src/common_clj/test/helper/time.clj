(ns common-clj.test.helper.time
  (:require [common-clj.time.parser.core :as time.parser]
            [schema.core :as s]))

(s/defn valid-iso-8601? :- s/Bool
  [date :- s/Str]
  (try (-> (time.parser/wire->date date) boolean)
       (catch Exception _ex false)))
