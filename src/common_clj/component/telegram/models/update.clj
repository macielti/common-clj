(ns common-clj.component.telegram.models.update
  (:require [schema.core :as s]))

(s/defschema Update
  {:update/id      s/Int
   :update/chat-id s/Int
   :update/type    s/Keyword
   :update/message s/Str})