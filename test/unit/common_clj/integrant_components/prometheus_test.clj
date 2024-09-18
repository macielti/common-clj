(ns common-clj.integrant-components.prometheus_test
  (:require [clojure.test :refer :all]
            [common-clj.integrant-components.prometheus]
            [iapetos.core :as prometheus]
            [integrant.core :as ig]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as s])
  (:import (iapetos.registry IapetosRegistry)
           (io.prometheus.client Counter$Child)))

(def config {:common-clj.integrant-components.prometheus/prometheus {:metrics [(prometheus/counter :example/metric)]}})

(s/deftest prometheus-component-test
  (testing "That we can define prometheus component and start it"
    (let [system (ig/init config)]
      (is (match? {:common-clj.integrant-components.prometheus/prometheus {:registry #(= (type %) IapetosRegistry)}}
                  system))

      (is (match? #(= (type %) Counter$Child)
                  (-> system :common-clj.integrant-components.prometheus/prometheus :registry :example/metric))))))
