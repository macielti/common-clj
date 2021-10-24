(ns integration.get-component-core-content-test
  (:use [clojure pprint])
  (:require [com.stuartsierra.component :as component]
            [clojure.test :refer :all]
            [common-clj.component.helper.core :as component.helper]))

(def ^:private core-content {:name "Freddie Mercury Prateado"})

(defrecord ^:private ExampleComponent []
  component/Lifecycle
  (start [component]
    (assoc component :example-component core-content))

  (stop [component]
    (assoc component :example-component nil)))

(defn ^:private new-example-component []
  (->ExampleComponent))

(def ^:private system-example
  (component/system-map
    :example-component (new-example-component)))

(deftest get-component-content-test
  (testing "that we can get the core content of the component"
    (let [started-system (component/start system-example)]
      (is (= core-content
             (component.helper/get-component-content :example-component started-system))))))
