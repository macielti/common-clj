(ns common-clj.schema.core-test
  (:require [clojure.test :refer :all])
  (:require [common-clj.schema.core :refer [loose-schema]]
            [schema.core :as s])
  (:import (clojure.lang ExceptionInfo)))

(s/defschema InnerSchema
  {:hi-lorena s/Str})

(s/defschema LooseSchemaExample
  {:a         s/Str
   :b         s/Keyword
   :more-here InnerSchema})

(def valid-value {:a         "a"
                  :b         :b
                  :more-here {:hi-lorena "ednaldo-pereira"}})

(deftest loose-schema-test
  (testing "a loose schema should not raise exception with mare then complete schema"
    (is (= (assoc valid-value :c "testando")
           (s/validate (loose-schema LooseSchemaExample) (assoc valid-value :c "testando")))))

  (testing "the inner maps of the defined schema should also becomes loose"
    (is (= {:a         "a"
            :b         :b
            :more-here {:hi-lorena "ednaldo-pereira"}
            :c         "c"}
           (s/validate (loose-schema LooseSchemaExample) (assoc-in valid-value [:more-here :greeting] "hi lorena")))))

  (testing "a loose schema should raise exception with less then complete schema"
    (is (thrown? ExceptionInfo (s/validate (loose-schema LooseSchemaExample) {:a "a"
                                                                              :c "c"})))))
