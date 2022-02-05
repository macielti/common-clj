(ns common-clj.component.telegram.models.consumer
  (:require [schema.core :as s])
  (:import (clojure.lang IFn)))


(s/defschema Consumer
  {(s/optional-key :consumer/interceptors)  [s/Keyword]
   :consumer/handler                        IFn
   (s/optional-key :consumer/error-handler) IFn})

(s/defschema Consumers
  {(s/optional-key :interceptors) [s/Any]
   :message                       {s/Keyword Consumer}
   :callback-query                {s/Keyword Consumer}})
