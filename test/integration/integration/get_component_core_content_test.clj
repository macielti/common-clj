(ns integration.get-component-core-content-test
  (:require [clojure.test :refer [is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.component.helper.core :as component.helper]
            [schema.test :as s]))

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

(s/deftest get-component-content-test
  (testing "that we can get the core content of the component"
    (let [started-system (component/start system-example)]
      (is (= core-content
             (component.helper/get-component-content :example-component started-system))))))
