(defproject net.clojars.macielti/common-clj "1.8.2"
  :description "Just common Clojure code that I use across projects"
  :url "https://github.com/macielti/common-clj"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :plugins [[lein-codox "0.10.8"]
            [lein-cloverage "1.2.2"]]

  :codox {:metadata    {:doc "Just common Clojure code that I use across projects."}
          :output-path "docs"}

  :dependencies [[com.attendify/schema-refined "0.3.0-alpha4"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 [medley "1.3.0"]
                 [telegrambot-lib "1.2.0"]
                 [overtone/at-at "1.2.0"]
                 [io.pedestal/pedestal.service "0.5.9"]
                 [prismatic/schema-generators "0.1.3"]
                 [nubank/matcher-combinators "3.3.1"]
                 [com.datomic/datomic-free "0.9.5697"]
                 [io.pedestal/pedestal.jetty "0.5.9"]
                 [io.pedestal/pedestal.route "0.5.7"]
                 [com.stuartsierra/component "1.0.0"]
                 [org.clojure/tools.logging "1.2.3"]
                 [org.clojure/clojure "1.10.3"]
                 [prismatic/plumbing "0.6.0"]
                 [camel-snake-kebab "0.4.2"]
                 [prismatic/schema "1.2.0"]
                 [cheshire "5.10.1"]]

  :test-paths ["test/unit" "test/integration" "test/helpers"]

  :repl-options {:init-ns common-clj.schema.core})
