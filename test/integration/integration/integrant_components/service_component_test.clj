(ns integration.integrant-components.service-component-test
  (:require [clojure.test :refer :all]
            [common-clj.integrant-components.routes]
            [common-clj.integrant-components.service]
            [integrant.core :as ig]
            [integration.aux.http :as aux.http]
            [schema.test :as s]))

(def routes [["/hello-world" :get (fn [{:keys [components]}]
                                    {:status 200 :body {:hello          "world"
                                                        :component-keys (keys components)}})
              :route-name :fetch-hello-world]])

(def config
  {:common-clj.integrant-components.routes/routes   {:routes routes}
   :common-clj.integrant-components.service/service {:components {:config {:service {:host "0.0.0.0"
                                                                                     :port 8080}}
                                                                  :routes (ig/ref :component/routes)}}})

(s/deftest service-component-test
  (testing "That we can request the defined endpoints"
    (let [system (ig/init config)
          service-fn (-> system :common-clj.integrant-components.service/service :io.pedestal.http/service-fn)]
      (is (= {:status 200
              :body   {:component-keys ["config" "routes"]
                       :hello          "world"}}
             (aux.http/request-test-endpoints "/hello-world" nil service-fn)))
      (ig/halt! system))))
