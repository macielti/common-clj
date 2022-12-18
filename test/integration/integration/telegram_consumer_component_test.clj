(ns integration.telegram-consumer-component-test
  (:require [clojure.test :refer [is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.routes :as component.routes]
            [common-clj.component.service :as component.service]
            [common-clj.component.telegram.consumer :as component.telegram.consumer]
            [schema.test :as s]
            [common-clj.component.helper.core :as component.helper]
            [io.pedestal.interceptor :as interceptor]))

(def test-state (atom nil))
(def interceptor-test-state (atom nil))

(def test-interceptor (interceptor/interceptor {:name  :test-interceptor
                                                :enter (fn [context]
                                                         (reset! interceptor-test-state (:update context))
                                                         context)}))

(defn test-consumer!
  [{:keys [update]}]
  (reset! test-state update))

(def consumers
  {:interceptors [test-interceptor]
   :message      {:test {:consumer/interceptors [:test-interceptor]
                         :consumer/handler      test-consumer!}}})

(def system-test
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test :json)
    :telegram-consumer (component/using (component.telegram.consumer/new-mock-telegram-consumer consumers) [:config])))

(s/deftest telegram-consumer-component-test
  (let [system (component/start system-test)
        telegram-consumer (component.helper/get-component-content :telegram-consumer system)]

    (testing "That we can consume a telegram bot command"
      (component.telegram.consumer/insert-incoming-update! {:message {:text "/test param1 param2"}} telegram-consumer)
      (Thread/sleep 5000)
      (is (= {:message {:text "/test param1 param2"}}
             @test-state))

      (is (= {:message {:text "/test param1 param2"}}
             @interceptor-test-state))

      (reset! test-state nil)
      (reset! interceptor-test-state nil))

    (testing "Not recognized telegram bot command is not consumed"

      (component.telegram.consumer/insert-incoming-update! {:message {:text "/wring-command param1 param2"}} telegram-consumer)
      (Thread/sleep 5000)
      (is (= nil
             @test-state)))

    (component/stop system)))

(def routes [["/api/handler" :post [(component.telegram.consumer/telegram-bot-webhook-endpoint-handler-fn consumers)]
              :route-name :telegram-bot-handler]])

(def system-test-using-telegram-webhook-consumer
  (component/system-map
    :config (component.config/new-config "resources/config_test.json" :test :json)
    :routes (component/using (component.routes/new-routes routes) [:config])
    :service (component/using (component.service/new-service) [:config :routes])))

(s/deftest telegram-consumer-webhook-test
  (let [system (component/start system-test-using-telegram-webhook-consumer)
        service-fn (-> (component.helper/get-component-content :service system)
                       :io.pedestal.http/service-fn)]

    (testing "That we can consume a telegram bot command via webhook"

      (is (= nil
             @test-state))

      (component.telegram.consumer/consume-update-via-webhook {:message {:text "/test param1 param2"}}
                                                              "/api/handler"
                                                              service-fn)

      (is (= {:message {:text "/test param1 param2"}}
             @test-state))

      (is (= {:message {:text "/test param1 param2"}}
             @interceptor-test-state))

      (reset! test-state nil)
      (reset! interceptor-test-state nil))

    (testing "Not recognized telegram bot command is not consumed"

      (component.telegram.consumer/consume-update-via-webhook {:message {:text "/wrong-command param1 param2"}}
                                                              "/api/handler"
                                                              service-fn)

      (Thread/sleep 5000)
      (is (= nil
             @test-state))

      (reset! test-state nil)
      (reset! interceptor-test-state nil))

    (component/stop system)))