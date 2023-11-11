(ns common-clj.keyword.core
  (:require [camel-snake-kebab.core :refer [->kebab-case]]
            [schema.core :as s]))

(s/defn str->keyword-kebab-case :- s/Keyword
  [k :- s/Str]
  (-> k
      ->kebab-case
      keyword))

(s/defn un-namespaced :- s/Keyword
  [x :- s/Keyword]
  (-> (name x)
      keyword))
