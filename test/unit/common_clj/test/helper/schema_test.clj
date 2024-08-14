(ns common-clj.test.helper.schema-test
  (:require [clojure.test :refer [is testing]]
            [common-clj.test.helper.schema :as test.helper.schema]
            [java-time.api :as jt]
            [matcher-combinators.test :refer [match?]]
            [schema.core :as schema]
            [schema.test :as s])
  (:import (java.time LocalDate LocalDateTime)
           (java.util Date)))

(schema/defschema SchemaTest
  {:a               schema/Str
   :b               schema/Keyword
   :date            Date
   :local-date-time LocalDateTime
   :local-date      LocalDate})

(s/deftest generate-test
  (testing "Given a schema and overrides, we can generate a map that matches the schema"
    (is (match? {:a               "a"
                 :b               keyword?
                 :date            inst?
                 :local-date-time jt/local-date-time?
                 :local-date      jt/local-date?}
                (test.helper.schema/generate SchemaTest {:a "a"})))))
