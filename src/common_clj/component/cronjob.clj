(ns common-clj.component.cronjob
  (:require [com.stuartsierra.component :as component]
            [hara.io.scheduler :as io.scheduler]
            [medley.core :as medley]
            [schema.core :as s]))

(s/defn ^:deprecated tasks-with-components
  [tasks components]
  (reduce (fn [tasks' task-id] (update-in tasks' [task-id :params] #(merge % {:components components}))) tasks (keys tasks)))

(defrecord ^:deprecated ConJob [config http-client prometheus tasks]
  component/Lifecycle
  (start ^:deprecated [component]
    (let [tasks' (tasks-with-components (:tasks tasks) (medley/assoc-some {}
                                                                          :config (:config config)
                                                                          :http-client (:http-client http-client)
                                                                          :prometheus (:prometheus prometheus)))
          scheduler (io.scheduler/scheduler tasks' {} {:clock {:timezone "UTC"}})]

      (io.scheduler/start! scheduler)

      (merge component {:jobs {:scheduler scheduler}})))

  (stop ^:deprecated [component]
    component))

(defn ^:deprecated new-cronjob
  [tasks]
  (->ConJob {} {} {} {:tasks tasks}))
