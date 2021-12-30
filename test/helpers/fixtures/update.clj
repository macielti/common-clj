(ns fixtures.update)

(def chat-id 123456789)

(def update-id 9876543221)

(def update-with-test-command-call
  {:update_id update-id
   :message   {:chat {:id chat-id}
               :text "/test testing"}})

(def update-with-callback-query
  {:update_id      update-id
   :callback_query {:message {:chat {:id chat-id}}
                    :data    "{\"handler\":\"callback-query\"}"}})
