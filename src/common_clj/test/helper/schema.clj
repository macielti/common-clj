(ns common-clj.test.helper.schema
  (:require [java-time.api :as jt]
            [schema-generators.complete :as c]
            [schema.core :as s])
  (:import (java.time LocalDate LocalDateTime)
           (java.util Date)))

(def leaf-generators
  {LocalDateTime (clojure.test.check.generators/fmap #(jt/local-date-time %) (clojure.test.check.generators/choose 2000 2024))
   LocalDate     (clojure.test.check.generators/fmap #(jt/local-date %) (clojure.test.check.generators/choose 2000 2024))
   Date          (clojure.test.check.generators/fmap #(jt/java-date %) (clojure.test.check.generators/choose 2000 2024))})

(s/defn generate :- s/Any
  [schema :- s/Any
   overrides :- (s/pred map?)]
  (c/complete overrides
              schema
              {}
              leaf-generators))