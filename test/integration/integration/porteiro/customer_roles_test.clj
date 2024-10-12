(ns integration.porteiro.customer-roles-test
  (:require [clojure.test :refer :all]
            [integrant.core :as ig]
            [integration.porteiro.aux :as porteiro.aux]
            [integration.porteiro.fixtures.customer :as fixtures.customer]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as s]))

(s/deftest customer-add-role
  (let [system (ig/init porteiro.aux/config)
        service-fn (-> system :common-clj.integrant-components.service/service :io.pedestal.http/service-fn)
        {{{:keys [id]} :customer} :body} (porteiro.aux/create-customer! fixtures.customer/customer-creation-document service-fn)
        {{:keys [token]} :body} (porteiro.aux/authenticate-customer! fixtures.customer/admin-customer-authentication-document service-fn)]

    (testing "That we are able to add a role to a customer"
      (is (match? {:status 200
                   :body   {:customer {:id       string?
                                       :username "ednaldo-pereira"
                                       :roles    ["test"]}}}
                  (porteiro.aux/add-role-to-customer! id :test token service-fn))))

    (ig/halt! system)))

(s/deftest customer-add-role-not-admin
  (let [system (ig/init porteiro.aux/config)
        service-fn (-> system :common-clj.integrant-components.service/service :io.pedestal.http/service-fn)
        {{{:keys [id]} :customer} :body} (porteiro.aux/create-customer! fixtures.customer/customer-creation-document service-fn)
        {{:keys [token]} :body} (porteiro.aux/authenticate-customer! fixtures.customer/customer-authentication-document service-fn)]

    (testing "That we are able to add a role to a customer"
      (is (match? {:status 403
                   :body   {:error   "insufficient-roles"
                            :message "Insufficient privileges/roles/permission"
                            :detail  "Insufficient privileges/roles/permission"}}
                  (porteiro.aux/add-role-to-customer! id :test token service-fn))))

    (ig/halt! system)))
