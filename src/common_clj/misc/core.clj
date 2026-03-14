(ns common-clj.misc.core
  (:require [clojure.walk :refer [postwalk]]
            [common-clj.keyword.core :as common-keyword]
            [schema.core :as s]))

(s/defn un-namespaced :- (s/pred map?)
  "Recursively un-namespace map keys"
  [schema :- (s/pred map?)]
  (postwalk (fn [x]
              (if (keyword? x)
                (common-keyword/un-namespaced x)
                x)) schema))

(defn- namespace-keys [m ns]
  (into {} (map (fn [[k v]] [(common-keyword/namespaced k ns) v])) m))

(s/defn namespaced :- (s/pred map?)
  "Recursively namespace map keys with the given namespace string"
  [m :- (s/pred map?)
   ns :- s/Str]
  (postwalk #(cond-> % (map? %) (namespace-keys ns)) m))
