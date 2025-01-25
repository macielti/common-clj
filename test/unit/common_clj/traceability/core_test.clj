(ns common-clj.traceability.core-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.traceability.core :as common-traceability]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.interceptor.chain :as chain]
            [matcher-combinators.test :refer [match?]]
            [mockfn.macros :as mfn]
            [schema.test :as s]))

(s/deftest reset-correlation-id!-test
  (common-traceability/reset-correlation-id!)
  (testing "Given an existing correlation-id, we should be able to reset it to nil"
    (common-traceability/correlation-id-appended!)
    (is (match? string?
                (common-traceability/correlation-id-appended!)))
    (is (match? string?
                @common-traceability/*correlation-id*))
    (common-traceability/reset-correlation-id!)
    (is (nil? @common-traceability/*correlation-id*))))

(s/deftest set-correlation-id!-test
  (common-traceability/reset-correlation-id!)
  (testing "Should be able to explicitly set the correlation-id"
    (is (= "DEFAULT.TEST"
           (common-traceability/set-correlation-id! "default.test")))
    (is (= "EDNALDO.PEREIRA"
           (common-traceability/set-correlation-id! "ednaldo.pereira")))))

(s/deftest current-correlation-id!-test
  (common-traceability/reset-correlation-id!)
  (testing "Given a empty correlation-id, we should get a default one"
    (is (= "DEFAULT"
           (common-traceability/current-correlation-id!))))
  (testing "Given a set correlation-id, we should get the same one"
    (common-traceability/set-correlation-id! "ednaldo.pereira")
    (is (= "EDNALDO.PEREIRA"
           (common-traceability/current-correlation-id!)))))

(s/deftest correlation-id-appended!-test
  (common-traceability/reset-correlation-id!)
  (mfn/providing [(random-uuid) #uuid "7835733a-83e1-46d1-94e4-7b84fb601d64"]
                 (is (= "DEFAULT.7B84FB601D64"
                        (common-traceability/correlation-id-appended!)))
                 (is (= "DEFAULT.7B84FB601D64.7B84FB601D64"
                        (common-traceability/correlation-id-appended!)))))

(s/deftest current-correlation-id-from-request-context-test
  (common-traceability/reset-correlation-id!)
  (is (= "DEFAULT.TEST"
         (common-traceability/correlation-id-from-request-context
          {:headers {"x-correlation-id" "default.test"}})))
  (common-traceability/reset-correlation-id!)
  (mfn/providing [(random-uuid) #uuid "7835733a-83e1-46d1-94e4-7b84fb601d64"]
                 (is (= "DEFAULT"
                        (common-traceability/correlation-id-from-request-context
                         {:headers {}})))))

(defn handler-fn->interceptor
  [handler-fn]
  (interceptor/interceptor
   {:name  ::test-interceptor
    :enter handler-fn}))

(def test-state (atom nil))

(s/deftest with-correlation-id-http-interceptor-test
  (testing "Given a HTTP request, we should be able to receive a correlation-id header"
    (chain/execute {:request {:headers {"x-correlation-id" "INTERCEPTOR_HTTP_TEST.1998"}}}
                   [common-traceability/with-correlation-id-http-interceptor
                    (handler-fn->interceptor (fn [context]
                                               (reset! test-state (common-traceability/current-correlation-id!))
                                               context))])
    (is (= "INTERCEPTOR_HTTP_TEST.1998"
           @test-state)))

  (testing "Given a HTTP request, we should be able to receive a correlation-id header - No conflict with main thread"
    (chain/execute {:request {:headers {"x-correlation-id" "INTERCEPTOR_HTTP_TEST.1998.V2"}}}
                   [common-traceability/with-correlation-id-http-interceptor
                    (handler-fn->interceptor (fn [context]
                                               (reset! test-state (common-traceability/current-correlation-id!))
                                               context))])
    (is (= "INTERCEPTOR_HTTP_TEST.1998.V2"
           @test-state)))

  (testing "Given a HTTP request, we should be able to receive a correlation-id header - Empty header"
    (chain/execute {:request {:headers {}}}
                   [common-traceability/with-correlation-id-http-interceptor
                    (handler-fn->interceptor (fn [context]
                                               (reset! test-state (common-traceability/current-correlation-id!))
                                               context))])
    (is (= "DEFAULT"
           @test-state))))

(s/deftest with-correlation-rabbitmq-interceptor-test
  (testing "Given a incoming rabbitmq message, we should be able to receive a correlation-id from the metadata"
    (chain/execute {:payload {:meta {:correlation-id "INTERCEPTOR_RABBITMQ_TEST.1998"}}}
                   [common-traceability/with-correlation-id-rabbitmq-interceptor
                    (handler-fn->interceptor (fn [context]
                                               (reset! test-state (common-traceability/current-correlation-id!))
                                               context))])
    (is (= "INTERCEPTOR_RABBITMQ_TEST.1998"
           @test-state)))

  (testing "Given a incoming rabbitmq message, we should be able to receive a correlation-id from the metadata - No conflict with main thread"
    (chain/execute {:payload {:meta {:correlation-id "INTERCEPTOR_RABBITMQ_TEST.1998.V2"}}}
                   [common-traceability/with-correlation-id-rabbitmq-interceptor
                    (handler-fn->interceptor (fn [context]
                                               (reset! test-state (common-traceability/current-correlation-id!))
                                               context))])
    (is (= "INTERCEPTOR_RABBITMQ_TEST.1998.V2"
           @test-state)))

  (testing "Given a incoming rabbitmq message, we should be able to receive a correlation-id from the metadata - Empty metadata map"
    (chain/execute {:payload {:meta {}}}
                   [common-traceability/with-correlation-id-rabbitmq-interceptor
                    (handler-fn->interceptor (fn [context]
                                               (reset! test-state (common-traceability/current-correlation-id!))
                                               context))])
    (is (= "DEFAULT"
           @test-state))))

(s/deftest with-correlation-job-interceptor-test
  (testing "Given a job execution, we should be able to identify logs from it using a job id"
    (chain/execute {}
                   [(common-traceability/with-correlation-id-job-interceptor "JOB_X.1998")
                    (handler-fn->interceptor (fn [context]
                                               (reset! test-state (common-traceability/current-correlation-id!))
                                               context))])
    (is (= "JOB_X.1998"
           @test-state)))

  (testing "Given a job execution, we should be able to identify logs from it using a job id - No conflict with main thread"
    (chain/execute {}
                   [(common-traceability/with-correlation-id-job-interceptor "JOB_Y.1998")
                    (handler-fn->interceptor (fn [context]
                                               (reset! test-state (common-traceability/current-correlation-id!))
                                               context))])
    (is (= "JOB_Y.1998"
           @test-state))))
