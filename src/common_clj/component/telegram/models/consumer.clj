(ns common-clj.component.telegram.models.consumer
  (:require [schema.core :as s])
  (:import (clojure.lang IFn)
           (io.pedestal.interceptor Interceptor)))

(s/defschema Consumer
  {(s/optional-key :consumer/interceptors)  [s/Keyword]
   :consumer/handler                        IFn
   (s/optional-key :consumer/error-handler) IFn})

(s/defschema Consumers
  {(s/optional-key :interceptors) [Interceptor]
   s/Keyword                      Consumer})
