(ns common-clj.schema.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [common-clj.schema.core :as common-core-schema]
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

(s/defschema NamespacedInnerSchema
  {:inner-namespaced/test-inner-key s/Keyword})

(s/defschema NamespacedSchema
  {:namespace/test-key                           s/Keyword
   (s/optional-key :namespace/test-optional-key) s/Keyword
   :namespace/test-inner-map                     NamespacedInnerSchema})

(def valid-map-for-un-namespaced-schema
  {:test-key          :ednaldo-pereira
   :test-optional-key :mamuel-gomes
   :test-inner-map    {:test-inner-key :galo-cego}})

(deftest loose-schema-test

  (testing "should be able to validate schemas"
    (is (= valid-value
           (s/validate (common-core-schema/loose-schema LooseSchemaExample) valid-value))))

  (testing "a loose schema should not raise exception with mare then complete schema"
    (is (= (assoc valid-value :c "testando")
           (s/validate (common-core-schema/loose-schema LooseSchemaExample) (assoc valid-value :c "testando")))))

  (testing "the inner maps of the defined schema should also becomes loose"
    (is (= (update valid-value :more-here assoc :greeting "hi lorena")
           (s/validate (common-core-schema/loose-schema LooseSchemaExample) (update valid-value :more-here assoc :greeting "hi lorena")))))

  (testing "a loose schema should raise exception with less then complete schema"
    (is (thrown? ExceptionInfo (s/validate (common-core-schema/loose-schema LooseSchemaExample) {:a "a"
                                                                                                 :c "c"})))))

(def telegram
  {:token                                  s/Str
   (s/optional-key :poll-interval-seconds) s/Int
   (s/optional-key :poll-timeout-seconds)  s/Int})

(s/defschema Telegram
  (common-core-schema/loose-schema telegram))

(def config
  {:telegram Telegram})

(s/defschema Config
  (common-core-schema/loose-schema config))

(deftest loose-schema-with-nested-loose-schema-test

  (testing "should validate a config with only required telegram fields"
    (is (= {:telegram {:token "my-token"}}
           (s/validate Config {:telegram {:token "my-token"}}))))

  (testing "should validate a config with optional telegram fields"
    (is (= {:telegram {:token                  "my-token"
                       :poll-interval-seconds  10
                       :poll-timeout-seconds   30}}
           (s/validate Config {:telegram {:token                  "my-token"
                                          :poll-interval-seconds  10
                                          :poll-timeout-seconds   30}}))))

  (testing "should accept extra keys in telegram (loose)"
    (is (= {:telegram {:token     "my-token"
                       :extra-key "extra-value"}}
           (s/validate Config {:telegram {:token     "my-token"
                                          :extra-key "extra-value"}}))))

  (testing "should accept extra keys in config (loose)"
    (is (= {:telegram  {:token "my-token"}
            :extra-key "extra-value"}
           (s/validate Config {:telegram  {:token "my-token"}
                               :extra-key "extra-value"}))))

  (testing "should reject config missing required telegram token"
    (is (thrown? ExceptionInfo (s/validate Config {:telegram {}})))))

(deftest un-namespaced-test
  (testing "GIVEN a namespaced schema WHEN I call un-namespaced on it THEN the schema should have only simple keywords"
    (is (= valid-map-for-un-namespaced-schema
           (s/validate (common-core-schema/un-namespaced NamespacedSchema) valid-map-for-un-namespaced-schema)))))
