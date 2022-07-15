(defproject net.clojars.macielti/common-clj "14.16.14"
  :description "Just common Clojure code that I use across projects"
  :url "https://github.com/macielti/common-clj"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :plugins [[lein-codox "0.10.8"]
            [lein-cloverage "1.2.3"]]

  :codox {:metadata    {:doc "Just common Clojure code that I use across projects."}
          :output-path "docs"}

  :exclusions [log4j]

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [com.attendify/schema-refined "0.3.0-alpha4"]
                 [nubank/mockfn "0.7.0"]
                 [morse "0.4.3"]
                 [overtone/at-at "1.2.0"]
                 [clj-test-containers "0.6.0"]
                 [datalevin "0.6.11"]
                 [org.apache.kafka/kafka-clients "2.8.0"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 [nubank/state-flow "5.14.1"]
                 [clj-http-fake "1.0.3"]
                 [medley "1.4.0"]
                 [telegrambot-lib "1.4.0"]
                 [overtone/at-at "1.2.0"]
                 [io.pedestal/pedestal.service "0.5.10"]
                 [io.pedestal/pedestal.interceptor "0.5.10"]
                 [prismatic/schema-generators "0.1.4"]
                 [nubank/matcher-combinators "3.5.0"]
                 [com.datomic/datomic-free "0.9.5697"]
                 [io.pedestal/pedestal.jetty "0.5.10"]
                 [io.pedestal/pedestal.route "0.5.10"]
                 [com.stuartsierra/component "1.1.0"]
                 [siili/humanize "0.1.1"]
                 [prismatic/plumbing "0.6.0"]
                 [camel-snake-kebab "0.4.3"]
                 [prismatic/schema "1.2.1"]
                 [buddy/buddy-sign "3.4.333"]
                 [cheshire "5.11.0"]]

  :resource-paths ["resources" "test/resources/"]

  :test-paths ["test/unit" "test/integration" "test/helpers"]

  :repl-options {:init-ns common-clj.schema.core})
