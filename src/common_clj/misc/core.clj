(ns common-clj.misc.core
  (:require [common-clj.keyword.core :as common-keyword]
            [schema.core :as s]
            [clojure.walk :refer [postwalk]]))

(s/defn un-namespaced :- (s/pred map?)
  "Recursively un-namespace map keys"
  [schema :- (s/pred map?)]
  (postwalk (fn [x]
              (if (keyword? x)
                (common-keyword/un-namespaced x)
                x)) schema))