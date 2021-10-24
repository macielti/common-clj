(defproject net.clojars.macielti/common-clj "1.6.2"
  :description "Just common Clojure code that I use across projects"
  :url "https://github.com/macielti/common-clj"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :plugins [[lein-codox "0.10.7"]
            [lein-cloverage "1.2.2"]]

  :codox {:metadata    {:doc "Just common Clojure code that I use across projects."}
          :output-path "docs"}

  :dependencies [[com.attendify/schema-refined "0.3.0-alpha4"]
                 [io.pedestal/pedestal.service "0.5.7"]
                 [prismatic/schema-generators "0.1.3"]
                 [com.datomic/datomic-free "0.9.5697"]
                 [io.pedestal/pedestal.jetty "0.5.7"]
                 [io.pedestal/pedestal.route "0.5.7"]
                 [com.stuartsierra/component "1.0.0"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.clojure/clojure "1.10.1"]
                 [prismatic/plumbing "0.5.5"]
                 [camel-snake-kebab "0.4.2"]
                 [prismatic/schema "1.1.11"]
                 [cheshire "5.10.0"]]

  :test-paths ["test/unit" "test/integration" "test/helpers"]

  :repl-options {:init-ns common-clj.schema.core})
