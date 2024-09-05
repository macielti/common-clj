(ns common-clj.component.helper.core
  (:require [schema.core :as s]))

(s/defn ^:deprecated get-component-content
  [component :- s/Keyword
   system]
  (get-in system [component component]))
