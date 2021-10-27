(ns common-clj.test.helper
  (:require [schema.core :as s])
  (:import (java.util UUID)))

(s/defn uuid :- s/Uuid
  []
  (UUID/randomUUID))
