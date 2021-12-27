(ns common-clj.component.telegram.adapters.message
  (:require [schema.core :as s]
            [clojure.string :as str]
            [common-clj.component.telegram.models.consumer :as component.telegram.models.consumer]
            [taoensso.timbre :as timbre]))

(s/defn message->command-type :- s/Keyword
  [message-text :- s/Str]
  (-> (re-find #"\S*" message-text)
      (str/replace #"\/" "")
      str/lower-case
      keyword))

(s/defn message->handler
  [message-text :- s/Str
   consumers :- component.telegram.models.consumer/Consumers]
  (let [command-type (message->command-type message-text)]
    (command-type consumers)))
