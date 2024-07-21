(defproject net.clojars.macielti/common-clj "25.51.50"
  :description "Just common Clojure code that I use across projects"
  :url "https://github.com/macielti/common-clj"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :plugins [[lein-codox "0.10.8"]
            [lein-cloverage "1.2.4"]
            [com.github.clojure-lsp/lein-clojure-lsp "1.4.9"]
            [com.github.liquidz/antq "RELEASE"]]

  :codox {:metadata    {:doc "Just common Clojure code that I use across projects."}
          :output-path "docs"}

  :exclusions [log4j]

  :dependencies [[org.clojure/clojure "1.11.3"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.4"]
                 [hashp "0.2.2"]
                 [com.attendify/schema-refined "0.3.0-alpha5"]
                 [nubank/mockfn "0.7.0"]
                 [morse "0.4.3"]
                 [overtone/at-at "1.3.58"]
                 [clj-test-containers "0.7.4"]
                 [datalevin "0.9.8"]
                 [org.apache.kafka/kafka-clients "3.7.1"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 [nubank/state-flow "5.17.0"]
                 [clj-http-fake "1.0.4"]
                 [medley "1.4.0"]
                 [telegrambot-lib "2.15.0"]
                 [io.pedestal/pedestal.service "0.7.0"]
                 [io.pedestal/pedestal.interceptor "0.7.0"]
                 [io.pedestal/pedestal.error "0.7.0"]
                 [io.pedestal/pedestal.jetty "0.7.0"]
                 [io.pedestal/pedestal.route "0.7.0"]
                 [prismatic/schema-generators "0.1.5"]
                 [nubank/matcher-combinators "3.9.1"]
                 [com.datomic/datomic-free "0.9.5697"]
                 [com.stuartsierra/component "1.1.0"]
                 [siili/humanize "0.1.1"]
                 [camel-snake-kebab "0.4.3"]
                 [prismatic/schema "1.4.1"]
                 [buddy/buddy-sign "3.5.351"]
                 [com.novemberain/langohr "5.4.0"]
                 [cheshire "5.13.0"]
                 [org.testcontainers/postgresql "1.19.8"]
                 [org.postgresql/postgresql "42.7.3"]
                 [com.github.seancorfield/next.jdbc "1.3.939"]
                 [com.datomic/local "1.0.285"]
                 [metosin/schema-tools "0.13.1"]
                 [clj-commons/iapetos "0.1.14"]
                 [clojure.java-time "1.4.2"]
                 [clj-rate-limiter "0.1.6-RC1"]
                 [com.github.liquidz/antq "RELEASE"]
                 [dev.weavejester/medley "1.8.0"]
                 [hara/io.scheduler "3.0.8"]]

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
