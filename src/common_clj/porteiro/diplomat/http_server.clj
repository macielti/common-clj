(ns common-clj.porteiro.diplomat.http-server
  (:require [common-clj.io.interceptors :as io.interceptors]
            [common-clj.io.interceptors.customer :as io.interceptors.customer]
            [common-clj.porteiro.diplomat.http-server.customer :as diplomat.http-server.customer]
            [common-clj.porteiro.interceptors.customer :as interceptors.customer]
            [common-clj.porteiro.wire.in.customer :as wire.in.customer]))

(def routes [["/api/customers" :post [(io.interceptors/schema-body-in-interceptor wire.in.customer/CustomerCreationDocument)
                                      interceptors.customer/username-already-in-use-interceptor
                                      diplomat.http-server.customer/create-customer!] :route-name :create-customer]

             ["/api/customers/auth" :post [(io.interceptors/schema-body-in-interceptor wire.in.customer/CustomerAuthenticationDocument)
                                           diplomat.http-server.customer/authenticate-customer!] :route-name :customer-authentication]

             ["/api/customers/roles" :post [io.interceptors.customer/identity-interceptor
                                            (io.interceptors.customer/required-roles-interceptor [:admin])
                                            diplomat.http-server.customer/add-role!] :route-name :add-role-to-customer]])
