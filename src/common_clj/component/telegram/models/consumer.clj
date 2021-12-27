(ns common-clj.component.telegram.models.consumer
  (:require [schema.core :as s])
  (:import (clojure.lang IFn)))

(s/defschema Consumer
  {:consumer/handler       IFn
   :consumer/error-handler IFn})
