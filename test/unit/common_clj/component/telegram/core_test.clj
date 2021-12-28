(ns common-clj.component.telegram.core-test
  (:require [clojure.test :refer :all]
            [schema.test :as s]
            [clj-http.fake :as fake]
            [telegrambot-lib.core :as telegram-bot]
            [common-clj.component.telegram.core :as component.telegram.core]
            [cheshire.core :as json]
            [io.pedestal.interceptor :as interceptor]))

(def test-state (atom nil))

(def chat-id 123456789)
(def token "123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11")
(def telegram (telegram-bot/create token))
(def components
  {:telegram telegram
   :config   {:telegram {:token                token
                         :chat-id              chat-id
                         :message-template-dir "templates"}}})
(def update {:update_id 123456789
             :message   {:chat {:id 123456789}
                         :text "/test testing"}})
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

(def dumb-interceptor
  (interceptor/interceptor
    {:name  :dumb-interceptor
     :enter (fn [_] nil)}))

(def consumers
  {:interceptors                   [auth-interceptor dumb-interceptor]
   :test                           {:consumer/interceptors  [:auth-interceptor]
                                    :consumer/handler       (fn [{:keys [message]}]
                                                              (swap! test-state assoc :text (:text message)))
                                    :consumer/error-handler (fn [_ _])}
   :with-exception-in-main-handler {:consumer/handler       (fn [_]
                                                              (throw (ex-info "Random exception"
                                                                              {:cause :nothing})))
                                    :consumer/error-handler (fn [exception _]
                                                              (reset! test-state (ex-data exception)))}
   :default-error-handler          {:consumer/handler (fn [_]
                                                        (throw (ex-info "Random exception"
                                                                        {:cause :nothing})))}})

(s/deftest consume-update!-test
  (testing "that we can consume a update"
    (component.telegram.core/consume-update! update consumers components)
    (is (= {:interceptor :auth-interceptor
            :text        "/test testing"}
           @test-state))
    (reset! test-state nil))
  (testing "that we can handle exception with error-handler provided by the user of the component"
    (component.telegram.core/consume-update! update-with-exception-in-command-consumption consumers components)
    (is (= :nothing
           (:cause @test-state)))
    (reset! test-state nil))
  (testing "that we can consume unmatched command messages"
    (fake/with-fake-routes
      {(format "https://api.telegram.org/bot%s/sendMessage" token) (fn [{:keys [body]}]
                                                                     (reset! test-state (-> (slurp body)
                                                                                            (json/parse-string true)
                                                                                            :text))
                                                                     {})}
      (component.telegram.core/consume-update! update-with-unmatched-command-message consumers components))
    (is (= "Sorry, command not found.\n"
           @test-state))
    (reset! test-state nil))
  (testing "that we can use a default error handler while consuming command messages"
    (fake/with-fake-routes
      {(format "https://api.telegram.org/bot%s/sendMessage" token) (fn [{:keys [body]}]
                                                                     (reset! test-state (-> (slurp body)
                                                                                            (json/parse-string true)))
                                                                     {})}
      (component.telegram.core/consume-update! update-default-error-handler consumers components))
    (is (= {:chat_id 123456789
            :text    "Sorry. An error occurred while processing your previous command.\n"}
           @test-state))
    (reset! test-state nil)))

(s/deftest send-message!-test
  (testing "that we can send telegram messages"
    (fake/with-fake-routes
      {(format "https://api.telegram.org/bot%s/sendMessage" token) (fn [{:keys [body]}]
                                                                     (reset! test-state (-> (slurp body)
                                                                                            (json/parse-string true)
                                                                                            :text))
                                                                     {})}
      (component.telegram.core/send-message! "Random message" components))
    (is (= "Random message"
           @test-state))
    (reset! test-state nil)))

(s/deftest commit-update-as-consumed!-test
  (testing "that we can commit consumed messages"
    (fake/with-fake-routes
      {(format "https://api.telegram.org/bot%s/getUpdates" token) (fn [{:keys [body]}]
                                                                    (reset! test-state (-> (slurp body)
                                                                                           (json/parse-string true)))
                                                                    {})}
      (component.telegram.core/commit-update-as-consumed! 123456789 (:telegram components)))
    (is (= {:offset 123456790}
           @test-state))))

(def consumer-interceptor-test {:consumer/interceptors [:auth-interceptor]
                                :consumer/handler      (fn [_] nil)})

(def consumers-with-interceptors {:interceptors              [auth-interceptor dumb-interceptor]
                                  :consumer-interceptor-test consumer-interceptor-test})

(s/deftest interceptors-by-consumer-test
  (testing "that we can get the correct interceptors specified by the consumer definition"
    (is (= [auth-interceptor]
           (component.telegram.core/interceptors-by-consumer consumer-interceptor-test consumers-with-interceptors)))))
