(ns common-clj.component.telegram.core-test
  (:require [clojure.test :refer :all]
            [schema.test :as s]
            [clj-http.fake :as fake]
            [telegrambot-lib.core :as telegram-bot]
            [common-clj.component.telegram.core :as component.telegram.core]
            [cheshire.core :as json]))

(def test-state (atom nil))

(def chat-id 123456789)
(def token "123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11")
(def telegram (telegram-bot/create token))
(def components
  {:telegram telegram
   :config   {:telegram {:token   token
                         :chat-id chat-id}}})
(def update {:update_id 123456789
             :message   {:chat {:id 123456789}
                         :text "/test testing"}})
(def update-with-exception-in-command-consumption {:update_id 123456800
                                                   :message   {:chat {:id 123456900}
                                                               :text "/with-exception-in-main-handler"}})
(def update-with-unmatched-command-message {:update_id 123456800
                                            :message   {:chat {:id 123456900}
                                                        :text "/unknown"}})
(def consumers
  {:test                           {:consumer/handler       (fn [message components]
                                                              (reset! test-state (:text message)))
                                    :consumer/error-handler (fn [exception components])}
   :with-exception-in-main-handler {:consumer/handler       (fn [message components]
                                                              (throw (ex-info "Random exception"
                                                                              {:cause :nothing})))
                                    :consumer/error-handler (fn [exception components]
                                                              (reset! test-state (ex-data exception)))}})

(s/deftest consume-update!-test
  (testing "that we can consume a update"
    (component.telegram.core/consume-update! update consumers components)
    (is (= "/test testing"
           @test-state))
    (reset! test-state nil))
  (testing "that we can handle exception with error-handler provided by the user of the component"
    (component.telegram.core/consume-update! update-with-exception-in-command-consumption consumers components)
    (is (= {:cause :nothing}
           @test-state))
    (reset! test-state nil))
  (testing "that we can consume unmatched command messages"
    (fake/with-fake-routes
      {(format "https://api.telegram.org/bot%s/sendMessage" token) (fn [{:keys [body] :as request}]
                                                                     (reset! test-state (-> (slurp body)
                                                                                            (json/parse-string true)
                                                                                            :text))
                                                                     {})}
      (component.telegram.core/consume-update! update-with-unmatched-command-message consumers components))
    (is (= "Sorry, command not found.\n"
           @test-state))
    (reset! test-state nil)))

(deftest send-message!-test
  (fake/with-fake-routes
    {(format "https://api.telegram.org/bot%s/sendMessage" token) (fn [{:keys [body] :as request}]
                                                                   (reset! test-state (-> (slurp body)
                                                                                          (json/parse-string true)
                                                                                          :text))
                                                                   {})}
    (component.telegram.core/send-message! "Random message" components))
  (is (= "Random message"
         @test-state))
  (reset! test-state nil))
