(ns common-clj.component.cronjob
  (:require [schema.core :as s]
            [com.stuartsierra.component :as component]
            [hara.io.scheduler :as io.scheduler]))

(s/defn tasks-with-components
  [tasks components]
  (reduce (fn [tasks' task-id] (update-in tasks' [task-id :params] #(merge % {:components components}))) tasks (keys tasks)))

(defrecord ConJob [config datalevin rabbitmq-producer tasks]
  component/Lifecycle
  (start [component]
    (let [tasks' (tasks-with-components (:tasks tasks) {:datalevin         (:datalevin datalevin)
                                                        :config            (:config config)
                                                        :rabbitmq-producer (:rabbitmq-producer rabbitmq-producer)})
          scheduler (io.scheduler/scheduler tasks' {} {:clock {:timezone "UTC"}})]

      (io.scheduler/start! scheduler)

      (merge component {:jobs {:scheduler scheduler}})))

  (stop [component]
    component))

(defn new-cronjob
  [tasks]
  (->ConJob {} {} {} {:tasks tasks}))