(defproject net.clojars.macielti/common-clj "25.51.48"
  :description "Just common Clojure code that I use across projects"
  :url "https://github.com/macielti/common-clj"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :plugins [[lein-codox "0.10.8"]
            [lein-cloverage "1.2.4"]
            [com.github.clojure-lsp/lein-clojure-lsp "1.4.4"]]

  :codox {:metadata    {:doc "Just common Clojure code that I use across projects."}
          :output-path "docs"}

  :exclusions [log4j]

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.4"]
                 [hashp "0.2.2"]
                 [com.attendify/schema-refined "0.3.0-alpha5"]
                 [nubank/mockfn "0.7.0"]
                 [morse "0.4.3"]
                 [overtone/at-at "1.3.58"]
                 [clj-test-containers "0.7.4"]
                 [datalevin "0.8.26"]
                 [org.apache.kafka/kafka-clients "3.6.1"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 [nubank/state-flow "5.14.5"]
                 [clj-http-fake "1.0.4"]
                 [medley "1.4.0"]
                 [telegrambot-lib "2.12.0"]
                 [overtone/at-at "1.2.0"]
                 [io.pedestal/pedestal.service "0.6.3"]
                 [io.pedestal/pedestal.interceptor "0.6.3"]
                 [prismatic/schema-generators "0.1.5"]
                 [nubank/matcher-combinators "3.9.1"]
                 [com.datomic/datomic-free "0.9.5697"]
                 [io.pedestal/pedestal.jetty "0.6.3"]
                 [io.pedestal/pedestal.route "0.6.3"]
                 [com.stuartsierra/component "1.1.0"]
                 [siili/humanize "0.1.1"]
                 [prismatic/plumbing "0.6.0"]
                 [camel-snake-kebab "0.4.3"]
                 [prismatic/schema "1.4.1"]
                 [buddy/buddy-sign "3.5.351"]
                 [com.novemberain/langohr "5.4.0"]
                 [cheshire "5.12.0"]
                 [org.testcontainers/postgresql "1.19.5"]
                 [org.postgresql/postgresql "42.7.1"]
                 [com.github.seancorfield/next.jdbc "1.3.909"]
                 [com.datomic/local "1.0.277"]
                 [metosin/schema-tools "0.13.1"]
                 [clj-commons/iapetos "0.1.13"]
                 [clojure.java-time "1.4.2"]
                 [clj-rate-limiter "0.1.5"]
                 [com.github.liquidz/antq "RELEASE"]]

  :injections [(require 'hashp.core)]

  :resource-paths ["resources" "test/resources/"]

  :test-paths ["test/unit" "test/integration" "test/helpers"]

  :aliases {"clean-ns"     ["clojure-lsp" "clean-ns" "--dry"] ;; check if namespaces are clean
            "format"       ["clojure-lsp" "format" "--dry"] ;; check if namespaces are formatted
            "diagnostics"  ["clojure-lsp" "diagnostics"]
            "lint"         ["do" ["clean-ns"] ["format"] ["diagnostics"]]
            "clean-ns-fix" ["clojure-lsp" "clean-ns"]
            "format-fix"   ["clojure-lsp" "format"]
            "lint-fix"     ["do" ["clean-ns-fix"] ["format-fix"]]
            "outdated"     ["with-profile" "antq" "run" "-m" "antq.core"]}

  :jvm-opts ^:replace ["--add-opens=java.base/java.nio=ALL-UNNAMED"
                       "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED"]

  :repl-options {:init-ns common-clj.schema.core})
