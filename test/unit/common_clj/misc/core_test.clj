(ns common-clj.misc.core-test
  (:require [clojure.test :refer :all]
            [common-clj.misc.core :as misc]
            [schema.test :as s]))

(s/deftest un-namespaced-test
  (testing "Given a map with namespaced keys, it should return a map with un-namespaced keys"
    (is (= {:a 1 :b 2}
           (misc/un-namespaced {:test/a 1 :test/b 2})))

    (is (= {:a 1 :b :key}
           (misc/un-namespaced {:test/a 1 :test/b :namespaced/key})))

    (is (= {:a 1 :b [:first :second]}
           (misc/un-namespaced {:test/a 1 :test/b [:namespaced/first :namespaced/second]})))))
