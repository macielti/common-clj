(ns integration.http-component-test
  (:require [clojure.test :refer :all]
            [schema.test :as s]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.routes :as component.routes]
            [common-clj.component.service :as component.service]
            [common-clj.component.http :as component.http]
            [cheshire.core :as json]
            [clj-http.fake :as http.fake]
            [common-clj.component.helper.core :as component.helper]
            [clj-http.client :as client]))

(def ^:private routes-example [["/test" :get (fn [{:keys [headers]}]
                                               {:status 200 :body headers})
                                :route-name :test]])

(def ^:private system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.edn" :test :edn)
    :http (component/using (component.http/new-http) [:config])
    :routes (component/using (component.routes/new-routes routes-example) [:config])
    :service (component/using (component.service/new-service) [:config :routes])))

(def mocked-http-responses
  {"https://example.com/users/auth"
   {:post (constantly {:status 200
                       :body   (json/encode {:token "random-token"})})}})

(s/deftest http-component-test
  (http.fake/with-fake-routes mocked-http-responses
    (let [system (component/start system-test)
          service-port (-> (component.helper/get-component-content :service system)
                           :io.pedestal.http/port)
          http (component.helper/get-component-content :http system)]
      (testing "that we can start a http component"
        (is (= {:authorization "random-token"}
               http)))
      (testing "that we can make a authorized request based on the http component"
        (is (= "random-token"
               (-> (client/get (format "http://localhost:%s/test" service-port) {:headers {"Authorization" (:authorization http)}})
                   :body
                   (json/decode true)
                   :authorization))))
      (component/stop-system system))))
