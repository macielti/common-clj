(ns common-clj.misc.core-test
  (:require [clojure.test :refer [is testing]]
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

(s/deftest namespaced-test
  (testing "Given a map with plain keys, it should return a map with namespaced keys"
    (is (= {:test/a 1 :test/b 2}
           (misc/namespaced {:a 1 :b 2} "test")))

    (testing "and keyword values should not be affected"
      (is (= {:test/a 1 :test/b :some-value}
             (misc/namespaced {:a 1 :b :some-value} "test"))))

    (testing "and nested map keys should also be namespaced"
      (is (= {:test/a {:test/b 2}}
             (misc/namespaced {:a {:b 2}} "test"))))))
