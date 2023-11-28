(ns common-clj.component.telegram.models.producer
  (:require [schema.core :as s])
  (:import (clojure.lang Atom)))

(def env #{:test :prod})
(def Env (apply s/enum env))

(s/defschema TelegramProducer
  {:token                     s/Str
   :current-env               Env
   (s/optional-key :produced) Atom})