(ns common-clj.component.telegram.models.update
  (:require [schema.core :as s]))

(s/defschema User
  {:user/id                          s/Int
   (s/optional-key :user/username)   s/Str
   (s/optional-key :user/first-name) s/Str
   (s/optional-key :user/last-name)  s/Str})

(s/defschema Update
  {:update/id                       s/Int
   :update/chat-id                  s/Int
   :update/type                     s/Keyword
   :update/message                  s/Str
   (s/optional-key :update/user)    User
   (s/optional-key :update/file-id) s/Str})
