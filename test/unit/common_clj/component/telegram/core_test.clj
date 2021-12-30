(ns common-clj.component.telegram.core-test
  (:require [clojure.test :refer :all]
            [schema.test :as s]
            [clj-http.fake :as fake]
            [cheshire.core :as json]
            [io.pedestal.interceptor :as interceptor]
            [fixtures.update]
            [fixtures.config]
            [fixtures.components]
            [fixtures.interceptors]
            [common-clj.component.telegram.core :as component.telegram.core]))

(def test-state (atom nil))

(def update-with-exception-in-command-consumption {:update_id 123456800
                                                   :message   {:chat {:id 123456900}
                                                               :text "/with-exception-in-main-handler"}})
(def update-with-unmatched-command-message {:update_id 123456800
                                            :message   {:chat {:id 123456900}
                                                        :text "/unknown"}})

(def update-default-error-handler {:update_id 123456800
                                   :message   {:chat {:id 123456900}
                                               :text "/default-error-handler"}})

(def auth-interceptor
  (interceptor/interceptor
    {:name  :auth-interceptor
     :enter (fn [context]
              (swap! test-state assoc :interceptor :auth-interceptor)
              context)}))

(def consumers
  {:interceptors   [auth-interceptor fixtures.interceptors/dumb-interceptor]
   :message        {:test                           {:consumer/interceptors  [:auth-interceptor]
                                                     :consumer/handler       (fn [{:keys [update]}]
                                                                               (swap! test-state assoc :text (-> update :message :text)))
                                                     :consumer/error-handler (fn [_ _])}
                    :with-exception-in-main-handler {:consumer/handler       (fn [_]
                                                                               (throw (ex-info "Random exception"
                                                                                               {:cause :nothing})))
                                                     :consumer/error-handler (fn [exception _]
                                                                               (reset! test-state (ex-data exception)))}
                    :default-error-handler          {:consumer/handler (fn [_]
                                                                         (throw (ex-info "Random exception"
                                                                                         {:cause :nothing})))}}
   :callback-query {:callback-query {:consumer/interceptors  [:auth-interceptor]
                                     :consumer/handler       (fn [{:keys [update]}]
                                                               (swap! test-state assoc :update update))
                                     :consumer/error-handler (fn [_ _])}}})

(s/deftest consume-update!-test
  (testing "that we can consume a update"
    (component.telegram.core/consume-update! fixtures.update/update-with-test-command-call consumers fixtures.components/components-for-telegram)
    (is (= {:interceptor :auth-interceptor
            :text        "/test testing"}
           @test-state))
    (reset! test-state nil))

  (testing "that we can handle/consume callback-queries"
    (component.telegram.core/consume-update! fixtures.update/update-with-callback-query consumers fixtures.components/components-for-telegram)
    (is (= {:interceptor :auth-interceptor
            :update      {:callback_query {:data    "{\"handler\":\"callback-query\"}"
                                           :message {:chat {:id 123456789}}}
                          :update_id      9876543221}}
           @test-state))
    (reset! test-state nil))
  (testing "that we can handle exception with error-handler provided by the user of the component"
    (component.telegram.core/consume-update! update-with-exception-in-command-consumption consumers fixtures.components/components-for-telegram)
    (is (= :nothing
           (:cause @test-state)))
    (reset! test-state nil))
  (testing "that we can consume unmatched command messages"
    (fake/with-fake-routes
      {(format "https://api.telegram.org/bot%s/sendMessage" fixtures.config/token) (fn [{:keys [body]}]
                                                                                     (reset! test-state (-> (slurp body)
                                                                                                            (json/parse-string true)
                                                                                                            :text))
                                                                                     {})}
      (component.telegram.core/consume-update! update-with-unmatched-command-message consumers fixtures.components/components-for-telegram))
    (is (= "Sorry, command not found.\n"
           @test-state))
    (reset! test-state nil))
  (testing "that we can use a default error handler while consuming command messages"
    (fake/with-fake-routes
      {(format "https://api.telegram.org/bot%s/sendMessage" fixtures.config/token) (fn [{:keys [body]}]
                                                                                     (reset! test-state (-> (slurp body)
                                                                                                            (json/parse-string true)))
                                                                                     {})}
      (component.telegram.core/consume-update! update-default-error-handler consumers fixtures.components/components-for-telegram))
    (is (= {:chat_id 123456789
            :text    "Sorry. An error occurred while processing your previous command.\n"}
           @test-state))
    (reset! test-state nil)))

(s/deftest send-message!-test
  (testing "that we can send telegram messages"
    (fake/with-fake-routes
      {(format "https://api.telegram.org/bot%s/sendMessage" fixtures.config/token) (fn [{:keys [body]}]
                                                                                     (reset! test-state (-> (slurp body)
                                                                                                            (json/parse-string true)
                                                                                                            :text))
                                                                                     {})}
      (component.telegram.core/send-message! "Random message" fixtures.components/components-for-telegram))
    (is (= "Random message"
           @test-state))
    (reset! test-state nil)))

(s/deftest commit-update-as-consumed!-test
  (testing "that we can commit consumed messages"
    (fake/with-fake-routes
      {(format "https://api.telegram.org/bot%s/getUpdates" fixtures.config/token) (fn [{:keys [body]}]
                                                                                    (reset! test-state (-> (slurp body)
                                                                                                           (json/parse-string true)))
                                                                                    {})}
      (component.telegram.core/commit-update-as-consumed! 123456789 fixtures.components/telegram))
    (is (= {:offset 123456790}
           @test-state))))

(def consumer-interceptor-test {:consumer/interceptors [:auth-interceptor]
                                :consumer/handler      (fn [_] nil)})

(def consumers-with-interceptors {:interceptors   [auth-interceptor fixtures.interceptors/dumb-interceptor]
                                  :message        {:consumer-interceptor-test consumer-interceptor-test}
                                  :callback-query {}})

(s/deftest interceptors-by-consumer-test
  (testing "that we can get the correct interceptors specified by the consumer definition"
    (is (= [auth-interceptor]
           (component.telegram.core/interceptors-by-consumer consumer-interceptor-test consumers-with-interceptors)))))
