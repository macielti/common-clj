(ns integration.porteiro.aux
  (:require [cheshire.core :as json]
            [common-clj.integrant-components.config]
            [common-clj.integrant-components.datomic]
            [common-clj.integrant-components.prometheus]
            [common-clj.integrant-components.routes]
            [common-clj.integrant-components.service]
            [common-clj.integrant-components.aws-auth]
            [common-clj.porteiro.admin]
            [integrant.core :as ig]
            [common-clj.porteiro.db.datomic.config :as database.config]
            [common-clj.porteiro.diplomat.http-server :as diplomat.http-server]
            [io.pedestal.test :as test]
            [schema.core :as s]))

(def config
  {:common-clj.integrant-components.config/config         {:path "resources/config.porteiro.edn"
                                                           :env  :test}
   :common-clj.integrant-components.datomic/datomic       {:schemas    database.config/schemas
                                                           :components {:config (ig/ref :common-clj.integrant-components.config/config)}}
   :common-clj.integrant-components.routes/routes         {:routes diplomat.http-server/routes}
   :common-clj.integrant-components.prometheus/prometheus {:metrics []}
   :common-clj.porteiro.admin/admin                       {:components {:config  (ig/ref :common-clj.integrant-components.config/config)
                                                                        :datomic (ig/ref :common-clj.integrant-components.datomic/datomic)}}
   :common-clj.integrant-components.service/service       {:components {:prometheus (ig/ref :common-clj.integrant-components.prometheus/prometheus)
                                                                        :config     (ig/ref :common-clj.integrant-components.config/config)
                                                                        :routes     (ig/ref :common-clj.integrant-components.routes/routes)
                                                                        :datomic    (ig/ref :common-clj.integrant-components.datomic/datomic)}}})

(defn create-customer!
  [customer
   service-fn]
  (let [{:keys [body status]} (test/response-for service-fn
                                                 :post "/api/customers"
                                                 :headers {"Content-Type" "application/json"}
                                                 :body (json/encode customer))]
    {:status status
     :body   (json/decode body true)}))

(defn authenticate-customer!
  [customer-authentication-document
   service-fn]
  (let [{:keys [body status]} (test/response-for service-fn
                                                 :post "/api/customers/auth"
                                                 :headers {"Content-Type" "application/json"}
                                                 :body (json/encode customer-authentication-document))]
    {:status status
     :body   (json/decode body true)}))

(s/defn add-role-to-customer!
  [customer-id
   role :- s/Keyword
   token
   service-fn]
  (let [{:keys [body status]} (test/response-for service-fn
                                                 :post (str "/api/customers/roles?customer-id=" customer-id "&role=" (name role))
                                                 :headers {"Content-Type"  "application/json"
                                                           "Authorization" (str "Bearer " token)})]
    {:status status
     :body   (json/decode body true)}))
