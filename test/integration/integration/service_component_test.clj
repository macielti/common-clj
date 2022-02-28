(ns integration.service-component-test
  (:require [clojure.test :refer :all]
            [common-clj.component.service :as component.service]
            [common-clj.component.config :as component.config]
            [common-clj.component.routes :as component.routes]
            [com.stuartsierra.component :as component]
            [common-clj.component.helper.core :as component.helper]
            [integration.aux.http :as aux.http]
            [common-clj.error.core :as common-error]))

(def ^:private routes-example [["/test" :get (fn [{{{:keys [datomic-uri]} :config} :components}]
                                               {:status 200 :body {:test "ok" :datomic-uri datomic-uri}})
                                :route-name :test]
                               ["/test-2" :get (fn [_] {:status 200 :body {:test-2 "ok"}}) :route-name :test-2]
                               ["/throw-info-exception" :get (fn [_] (common-error/http-friendly-exception 418
                                                                                                           "unknown"
                                                                                                           "I'm not a teapot"
                                                                                                           "Just chilling"))
                                :route-name :intentional-info-exception]
                               ["/throw-simple-exception" :get (fn [_] (throw (throw (Exception. "my exception message"))))
                                :route-name :intentional-simple-exception]])

(def ^:private system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test :json)
    :routes (component/using (component.routes/new-routes routes-example)
                             [:config])
    :service (component/using (component.service/new-service)
                              [:config :routes])))

(deftest service-component-test
  (let [system     (component/start system-test)
        service-fn (-> (component.helper/get-component-content :service system)
                       :io.pedestal.http/service-fn)]
    (testing "that we can request the defined endpoints"
      (is (= {:status 200
              :body   {:test-2 "ok"}}
             (aux.http/request-test-endpoints "/test-2" service-fn))))
    (testing "that we can access components content from request func handlers if we need to"
      (is (= {:status 200
              :body   {:datomic-uri "datomic:mem://example-test"
                       :test        "ok"}}
             (aux.http/request-test-endpoints "/test" service-fn))))
    (testing "that we can catch and present friendly error messages using the error interceptor"
      (is (= {:status 418
              :body   {:error   "unknown"
                       :message "I'm not a teapot"
                       :detail  "Just chilling"}}
             (aux.http/request-test-endpoints "/throw-info-exception" service-fn)))
      (is (= {:body   {:cause "Internal Server Error"}
              :status 500}
             (aux.http/request-test-endpoints "/throw-simple-exception" service-fn))))
    (component/stop-system system)))
