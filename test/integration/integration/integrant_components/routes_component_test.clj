(ns integration.integrant-components.routes-component-test
  (:require [clojure.test :refer :all]
            [integrant.core :as ig]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as s]))

(def routes [["/hello-world" :get (fn [{:keys [components]}]
                                    {:status 200 :body {:hello          "world"
                                                        :component-keys (keys components)}})
              :route-name :fetch-hello-world]])

(def config
  {:common-clj.integrant-components/routes {:routes routes}})

(s/deftest service-component-test
  (testing "That we can define endpoints"
    (let [system (ig/init config)]
      (is (match? #{["/hello-world"
                     :get function?
                     :route-name
                     :fetch-hello-world]}
                  (-> system :common-clj.integrant-components/routes)))
      (ig/halt! system))))