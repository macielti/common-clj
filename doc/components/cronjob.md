# Cronjob Component

Should be used when you need to perform a task at a specific time or at regular intervals.

## Usage

The first thing to do is define the task you want to run and add it to a tasks definition map:
```clojure
(defn example-task
  [_as-of
   {:keys [_components param-test] :as _params}
   _instance]
  (log/info ::example-task "Running example task" :param-test param-test))


(def tasks {:test-task {:handler  test-task
                        :schedule "* * * * * * *"
                        :params   {}}})
```

After that you need to add the cronjob component to your system map:
```clojure
(def system
  (component/system-map 
    :cronjob (component.cronjob/new-cronjob <<tasks>>)))
```