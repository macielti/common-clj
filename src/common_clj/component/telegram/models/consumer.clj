(ns common-clj.component.telegram.models.consumer
  (:require [schema.core :as s])
  (:import (clojure.lang IFn)))

(s/defschema Consumer
  {(s/optional-key :interceptors)  [s/Keyword]
   :handler                        IFn
   (s/optional-key :error-handler) IFn})

(s/defschema Consumers
  {(s/optional-key :interceptors) [s/Any]
   (s/optional-key :bot-command)  {s/Keyword Consumer}})
