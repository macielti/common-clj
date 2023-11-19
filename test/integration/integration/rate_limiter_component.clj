(ns integration.rate-limiter-component
  (:require [clojure.test :refer [is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.routes :as component.routes]
            [common-clj.component.service :as component.service]
            [common-clj.component.rate-limiter :as component.rate-limiter]
            [clj-rate-limiter.core :as r]
            [integration.aux.http :as aux.http]
            [io.pedestal.interceptor :as pedestal.interceptor]
            [common-clj.error.core :as common-error]
            [schema.test :as s]))


(def rate-limiters-definition
  {:1-per-min (r/rate-limiter-factory :memory
                                      :interval 60000
                                      :max-in-interval 1)
   :2-per-min (r/rate-limiter-factory :memory
                                      :interval 60000
                                      :max-in-interval 2)})

(def rate-limit-1-per-min-based-on-ip-interceptor
  (pedestal.interceptor/interceptor
    {:name  ::rate-limit-1-per-sec-based-on-ip-interceptor
     :enter (fn [{{:keys [components remote-addr]} :request :as context}]
              (let [{:keys [rate-limiter]} components]
                (when-not (r/allow? (get @rate-limiter :1-per-min) remote-addr)
                  (common-error/http-friendly-exception 429
                                                        "too-many-requests"
                                                        "Too Many Requests"
                                                        {:error :too-many-requests})))
              context)}))

(def rate-limit-2-per-min-based-on-ip-interceptor
  (pedestal.interceptor/interceptor
    {:name  ::rate-limit-2-per-sec-based-on-ip-interceptor
     :enter (fn [{{:keys [components remote-addr]} :request :as context}]
              (let [{:keys [rate-limiter]} components]
                (when-not (r/allow? (get @rate-limiter :2-per-min) remote-addr)
                  (common-error/http-friendly-exception 429
                                                        "too-many-requests"
                                                        "Too Many Requests"
                                                        {:error :too-many-requests})))
              context)}))

(def ^:private routes-example [["/test-rate-limit-1-per-min" :get [rate-limit-1-per-min-based-on-ip-interceptor
                                                                   (fn [_context]
                                                                     {:status 200 :body {:test "test-rate-limit-1-per-min"}})]
                                :route-name :test-rate-limit-1-per-min]

                               ["/test-rate-limit-2-per-min" :get [rate-limit-2-per-min-based-on-ip-interceptor
                                                                   (fn [_context]
                                                                     {:status 200 :body {:test "test-rate-limit-2-per-min"}})]
                                :route-name :test-rate-limit-5-per-min]])

(def ^:private system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test :json)
    :routes (component/using (component.routes/new-routes routes-example) [:config])
    :rate-limiter (component.rate-limiter/new-rate-limiter rate-limiters-definition)
    :service (component/using (component.service/new-service) [:config :routes :rate-limiter])))

(s/deftest rate-limit-component-test-1-per-minute
  (let [system (component/start system-test)
        service-fn (-> (component.helper/get-component-content :service system)
                       :io.pedestal.http/service-fn)]

    (testing "Given a rate limit of 1 per min, the first request in the 1 minute time window should pass"
      (is (= {:status 200
              :body   {:test "test-rate-limit-1-per-min"}}
             (aux.http/request-test-endpoints "/test-rate-limit-1-per-min" nil service-fn))))

    (testing "Given a rate limit of 1 per min, the second request in the 1 minute time window should be refused"
      (is (= {:status 429
              :body   {:detail  {:error "too-many-requests"}
                       :error   "too-many-requests"
                       :message "Too Many Requests"}}
             (aux.http/request-test-endpoints "/test-rate-limit-1-per-min" nil service-fn))))

    (Thread/sleep 60000)

    (testing "Given a rate limit of 1 per min is extrapolated we should be able to completed the request 1 minute later"
      (is (= {:status 200
              :body   {:test "test-rate-limit-1-per-min"}}
             (aux.http/request-test-endpoints "/test-rate-limit-1-per-min" nil service-fn))))

    (component/stop-system system)))

(s/deftest rate-limit-component-test-2-per-minute
  (let [system (component/start system-test)
        service-fn (-> (component.helper/get-component-content :service system)
                       :io.pedestal.http/service-fn)]

    (testing "Given a rate limit of 2 per min, the first request in the 1 minute time window should pass"
      (is (= {:status 200
              :body   {:test "test-rate-limit-2-per-min"}}
             (aux.http/request-test-endpoints "/test-rate-limit-2-per-min" nil service-fn))))

    (testing "Given a rate limit of 2 per min, the second request in the 1 minute time window should be fine"
      (is (= {:status 200
              :body   {:test "test-rate-limit-2-per-min"}}
             (aux.http/request-test-endpoints "/test-rate-limit-2-per-min" nil service-fn))))

    (testing "Given a rate limit of 2 per min, the third request in the 1 minute time window should be refused"
      (is (= {:status 429
              :body   {:detail  {:error "too-many-requests"}
                       :error   "too-many-requests"
                       :message "Too Many Requests"}}
             (aux.http/request-test-endpoints "/test-rate-limit-2-per-min" nil service-fn))))

    (Thread/sleep 60000)

    (testing "Given a rate limit of 2 per min, the fourth request in the 1 minute time window should be fine"
      (is (= {:status 200
              :body   {:test "test-rate-limit-2-per-min"}}
             (aux.http/request-test-endpoints "/test-rate-limit-2-per-min" nil service-fn))))

    (component/stop-system system)))
