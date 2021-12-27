(ns common-clj.component.telegram.adapters.message
  (:require [schema.core :as s]
            [clojure.string :as str]))

(s/defn message->command-type :- s/Keyword
  [message-text :- s/Str]
  (-> (re-find #"\S*" message-text)
      (str/replace #"\/" "")
      str/lower-case
      keyword))

(s/defn message->handler
  [message-text :- s/Str
   consumers]
  (let [command-type (message->command-type message-text)]
    (command-type consumers)))
