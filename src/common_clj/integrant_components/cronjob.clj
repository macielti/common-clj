(ns common-clj.integrant-components.cronjob
  (:require [hara.io.scheduler :as io.scheduler]
            [integrant.core :as ig]
            [schema.core :as s]
            [taoensso.timbre :as log]))

(s/defn tasks-with-components
  [tasks components]
  (reduce (fn [tasks' task-id] (update-in tasks' [task-id :params] #(merge % {:components components}))) tasks (keys tasks)))

(defmethod ig/init-key ::cronjob
  [_ {:keys [tasks components]}]
  (log/info :starting ::cronjob)
  (let [tasks' (tasks-with-components (:tasks tasks) components)
        scheduler (io.scheduler/scheduler tasks' {} {:clock {:timezone "UTC"}})]

    (io.scheduler/start! scheduler)

    {:scheduler scheduler}))

(defmethod ig/halt-key! ::cronjob
  [_ cronjob]
  (log/info :stopping ::cronjob)
  (io.scheduler/stop! (:scheduler cronjob)))
