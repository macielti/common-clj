(ns common-clj.integrant-components.routes-test
  (:require [clojure.test :refer [is testing function?]]
            [integrant.core :as ig]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as s]))

(def routes [["/hello-world" :get (fn [{:keys [components]}]
                                    {:status 200 :body {:hello          "world"
                                                        :component-keys (keys components)}})
              :route-name :fetch-hello-world]])

(def config
  {:common-clj.integrant-components.routes/routes {:routes routes}})

(s/deftest routes-component-definition-test
  (testing "That we can define route component and start it"
    (let [system (ig/init config)]
      (is (match? {:common-clj.integrant-components.routes/routes #{["/hello-world"
                                                                     :get function?
                                                                     :route-name :fetch-hello-world]}}
                  system))
      (is (nil? (ig/halt! system))))))
