(ns common-clj.schema.core-test
  (:require [clojure.test :refer :all])
  (:require [common-clj.schema.core :refer [loose-schema]]
            [schema.core :as s])
  (:import (clojure.lang ExceptionInfo)))

(s/defschema LooseSchemaExample
  {:a s/Str
   :b s/Keyword})

(deftest loose-schema-test
  (testing "a loose schema should not raise exception with mare then complete schema"
    (is (= {:a "a"
            :b :b
            :c "c"}
           (s/validate (loose-schema LooseSchemaExample) {:a "a"
                                                          :b :b
                                                          :c "c"}))))

  (testing "a loose schema should raise exception with less then complete schema"
    (is (thrown? ExceptionInfo (s/validate (loose-schema LooseSchemaExample) {:a "a"
                                                                             :c "c"})))))
