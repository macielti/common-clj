(ns integration.porteiro.customer-test
  (:require [clojure.test :refer :all]
            [integrant.core :as ig]
            [schema.test :as s]
            [integration.porteiro.aux :as porteiro.aux]
            [matcher-combinators.test :refer [match?]]
            [integration.porteiro.fixtures.customer :as fixtures.customer]))

(s/deftest customer-creation-test
  (let [system (ig/init porteiro.aux/config)
        service-fn (-> system :common-clj.integrant-components.service/service :io.pedestal.http/service-fn)]

    (testing "That we are able to create a customer"
      (is (match? {:status 201
                   :body   {:customer {:id       string?
                                       :username fixtures.customer/customer-username
                                       :roles    []}}}
                  (porteiro.aux/create-customer! fixtures.customer/customer-creation-document service-fn))))
    (ig/halt! system)))

(s/deftest customer-authentication
  (let [system (ig/init porteiro.aux/config)
        service-fn (-> system :common-clj.integrant-components.service/service :io.pedestal.http/service-fn)]

    (testing "That we are able to create a customer"
      (is (match? {:status 201
                   :body   {:customer {:id       string?
                                       :username fixtures.customer/customer-username
                                       :roles    []}}}
                  (porteiro.aux/create-customer! fixtures.customer/customer-creation-document service-fn))))

    (testing "That we are able to authenticate a customer"
      (is (match? {:status 200
                   :body   {:token string?}}
                  (porteiro.aux/authenticate-customer! fixtures.customer/customer-authentication-document service-fn))))

    (ig/halt! system)))

(s/deftest customer-authentication-wrong-credentials
  (let [system (ig/init porteiro.aux/config)
        service-fn (-> system :common-clj.integrant-components.service/service :io.pedestal.http/service-fn)]

    (testing "That we are able to create a customer"
      (is (match? {:status 201
                   :body   {:customer {:id       string?
                                       :username fixtures.customer/customer-username
                                       :roles    []}}}
                  (porteiro.aux/create-customer! fixtures.customer/customer-creation-document service-fn))))

    (testing "That we are able to authenticate a customer"
      (is (match? {:status 403
                   :body   {:error   "invalid-credentials"
                            :message "Wrong username or/and password"
                            :detail  "Customer is trying to login using invalid credentials"}}
                  (porteiro.aux/authenticate-customer! (assoc-in fixtures.customer/customer-authentication-document
                                                                 [:customer :password] "password-wrong") service-fn))))

    (testing "That we are able to authenticate a customer"
      (is (match? {:status 403
                   :body   {:error   "invalid-credentials"
                            :message "Wrong username or/and password"
                            :detail  "Customer is trying to login using invalid credentials"}}
                  (porteiro.aux/authenticate-customer! (assoc-in fixtures.customer/customer-authentication-document
                                                                 [:customer :username] "manuel-gomes") service-fn))))

    (ig/halt! system)))
