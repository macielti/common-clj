(ns common-clj.keyword.core
  (:require [schema.core :as s]
            [camel-snake-kebab.core :refer [->kebab-case]]))

(s/defn str->keyword-kebab-case :- s/Keyword
  [k :- s/Str]
  (-> k
      ->kebab-case
      keyword))
