(ns integration.cronjob-component-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [common-clj.component.cronjob :as component.cronjob]
            [schema.test :as s]))

(def test-state (atom nil))

(defn test-task
  [_as-of
   {:keys [_components param-test] :as _params}
   _instance]
  (reset! test-state param-test))

(def tasks {:test-task {:handler  test-task
                        :schedule "* * * * * * *"
                        :params   {:param-test :ok}}})

(def system-test
  (component/system-map
    :cronjob (component.cronjob/new-cronjob tasks)))

(s/deftest cronjob-task-execution-test
  (let [system (component/start system-test)]
    (testing "that cronjob task is executed"
      (reset! test-state nil)
      (Thread/sleep 1000)

      (is (= :ok @test-state))
      (component/stop-system system))))
