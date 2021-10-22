(defproject net.clojars.macielti/common-clj "0.2.1"
  :description "Just common Clojure code that I use across projects"
  :url "https://github.com/macielti/common-clj"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :plugins [[lein-codox "0.10.7"]
            [lein-cloverage "1.2.2"]]

  :codox {:metadata    {:doc "Just common Clojure code that I use across projects."}
          :output-path "docs"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [prismatic/schema "1.1.11"]]

  :test-paths ["test/unit" "test/integration" "test/helpers"]

  :repl-options {:init-ns common-clj.schema.core})
