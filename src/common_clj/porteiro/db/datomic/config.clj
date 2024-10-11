(ns common-clj.porteiro.db.datomic.config
  (:require [common-clj.porteiro.wire.datomic.customer :as wire.datomic.customer]))

(def schemas (concat []
                     wire.datomic.customer/customer))
