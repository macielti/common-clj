(ns integration.resource-existence-check-interceptor-test
  (:require [clojure.test :refer [is testing]]
            [com.stuartsierra.component :as component]
            [common-clj.component.config :as component.config]
            [common-clj.component.datomic :as component.datomic]
            [common-clj.component.helper.core :as component.helper]
            [common-clj.component.routes :as component.routes]
            [common-clj.component.service :as component.service]
            [common-clj.io.interceptors.datomic :as io.interceptors.datomic]
            [datomic.api :as d]
            [integration.aux.http :as aux.http]
            [schema.test :as s])
  (:import (java.util UUID)))

(def ^:private test-skeleton
  [{:db/ident       :test/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "Test id"}])

(def ^:private schemas [test-skeleton])

(defn resource-identifier-fn
  [{{:keys [path-params]} :request}]
  (-> path-params :id UUID/fromString))

(def resource-existence-interceptor-check
  (io.interceptors.datomic/resource-existence-check-interceptor resource-identifier-fn
                                                                '[:find (pull ?resource [*])
                                                                  :in $ ?resource-identifier
                                                                  :where [?resource :test/id ?resource-identifier]]))

(def ^:private routes-example [["/resource-existence-check-interceptor-test/:id" :get [resource-existence-interceptor-check
                                                                                       (fn [{{:keys [id]} :path-params}]
                                                                                         {:status 200 :body {:id id}})]
                                :route-name :resource-existence-check-interceptor-test]])

(def ^:private system-test
  (component/system-map
   :config (component.config/new-config "resources/config_test.json" :test :json)
   :routes (component/using (component.routes/new-routes routes-example) [:config])
   :datomic (component/using (component.datomic/new-datomic schemas) [:config])
   :service (component/using (component.service/new-service) [:config :datomic :routes])))

(s/deftest resource-existence-check-interceptor-test
  (let [system (component/start system-test)
        database-conn (component.helper/get-component-content :datomic system)
        service-fn (-> (component.helper/get-component-content :service system)
                       :io.pedestal.http/service-fn)
        resource-id (random-uuid)]
    (testing "that we can successfully make a post request respecting the expected schema for the body content"
      (d/transact database-conn [{:test/id resource-id}])
      (is (= {:status 200
              :body   {:id (str resource-id)}}
             (aux.http/request-test-endpoints (str "/resource-existence-check-interceptor-test/" resource-id) nil service-fn))))
    (component/stop-system system)))
