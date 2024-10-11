(ns common-clj.porteiro.diplomat.http-server
  (:require [common-clj.io.interceptors :as io.interceptors]
            [common-clj.porteiro.wire.in.customer :as wire.in.customer]
            [common-clj.porteiro.interceptors.customer :as interceptors.customer]
            [common-clj.porteiro.diplomat.http-server.customer :as diplomat.http-server.customer]))

(def routes [["/api/customers" :post [(io.interceptors/schema-body-in-interceptor wire.in.customer/CustomerCreationDocument)
                                      interceptors.customer/username-already-in-use-interceptor
                                      diplomat.http-server.customer/create-customer!] :route-name :create-customer]

             ["/api/customers/auth" :post [(io.interceptors/schema-body-in-interceptor wire.in.customer/CustomerAuthenticationDocument)
                                           diplomat.http-server.customer/authenticate-customer!] :route-name :customer-authentication]])
