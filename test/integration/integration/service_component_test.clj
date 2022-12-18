(ns integration.service-component-test
  (:require [clojure.test :refer [is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.routes :as component.routes]
            [common-clj.component.service :as component.service]
            [common-clj.error.core :as common-error]
            [integration.aux.http :as aux.http]
            [schema.test :as s]))

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
   :routes (component/using (component.routes/new-routes routes-example) [:config])
   :service (component/using (component.service/new-service)
                             [:config :routes])))

(s/deftest service-component-test
  (let [system     (component/start system-test)
        service-fn (-> (component.helper/get-component-content :service system)
                       :io.pedestal.http/service-fn)]
    (testing "that we can request the defined endpoints"
      (is (= {:status 200
              :body   {:test-2 "ok"}}
             (aux.http/request-test-endpoints "/test-2" nil service-fn))))
    (testing "that we can access components content from request func handlers if we need to"
      (is (= {:status 200
              :body   {:datomic-uri "datomic:mem://example-test"
                       :test        "ok"}}
             (aux.http/request-test-endpoints "/test" nil service-fn))))
    (testing "that we can catch and present friendly error messages using the error interceptor"
      (is (= {:status 418
              :body   {:error   "unknown"
                       :message "I'm not a teapot"
                       :detail  "Just chilling"}}
             (aux.http/request-test-endpoints "/throw-info-exception" nil service-fn)))
      (is (= {:status 500
              :body   {:detail  "Internal Server Error"
                       :error   "unexpected-server-error"
                       :message "Internal Server Error"}}
             (aux.http/request-test-endpoints "/throw-simple-exception" nil service-fn))))
    (component/stop-system system)))
