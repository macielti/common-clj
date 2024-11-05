(defproject net.clojars.macielti/common-clj "32.70.70"

  :description "Just common Clojure code that I use across projects"

  :url "https://github.com/macielti/common-clj"

  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :exclusions [log4j]

  :dependencies [[org.clojure/clojure "1.11.4"]
                 [com.attendify/schema-refined "0.3.0-alpha5"]
                 [morse "0.4.3"]
                 [overtone/at-at "1.3.58"]
                 [org.apache.kafka/kafka-clients "3.8.0"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 [telegrambot-lib "2.15.0"]
                 [io.pedestal/pedestal.service "0.7.2"]
                 [io.pedestal/pedestal.interceptor "0.7.2"]
                 [io.pedestal/pedestal.error "0.7.2"]
                 [io.pedestal/pedestal.jetty "0.7.2"]
                 [io.pedestal/pedestal.route "0.7.2"]
                 [prismatic/schema-generators "0.1.5"]
                 [org.clojure/core.async "1.6.681"]
                 [com.stuartsierra/component "1.1.0"]
                 [siili/humanize "0.1.1"]
                 [camel-snake-kebab "0.4.3"]
                 [prismatic/schema "1.4.1"]
                 [buddy/buddy-sign "3.6.1-359"]
                 [com.novemberain/langohr "5.5.0"]
                 [cheshire "5.13.0"]
                 [metosin/schema-tools "0.13.1"]
                 [clj-commons/iapetos "0.1.14"]
                 [clojure.java-time "1.4.2"]
                 [clj-rate-limiter "0.1.6-RC1"]
                 [dev.weavejester/medley "1.8.1"]
                 [hara/io.scheduler "3.0.12"]
                 [integrant "0.13.1"]
                 [amazonica "0.3.167"]
                 [com.fasterxml.jackson.core/jackson-core "2.18.1"]
                 [com.fasterxml.jackson.core/jackson-databind "2.18.1"]
                 [com.fasterxml.jackson.core/jackson-annotations "2.18.1"]
                 [diehard "0.11.12"]
                 [overtone/at-at "1.4.65"]
                 [buddy/buddy-hashers "1.8.158"]
                 [commons-io/commons-io "2.17.0"]
                 [net.clojars.macielti/postgresql-component "2.1.2"]
                 [com.taoensso/timbre "6.6.1"]]

  :profiles {:dev {:resource-paths ^:replace ["test/resources"]

                   :test-paths     ^:replace ["test/unit" "test/integration" "test/helpers"]

                   :plugins        [[lein-cloverage "1.2.4"]
                                    [com.github.clojure-lsp/lein-clojure-lsp "1.4.13"]
                                    [com.github.liquidz/antq "RELEASE"]]

                   :dependencies   [[net.clojars.macielti/common-test-clj "1.0.0"]
                                    [nubank/matcher-combinators "3.9.1"]
                                    [hashp "0.2.2"]]

                   :injections     [(require 'hashp.core)]

                   :aliases        {"clean-ns"     ["clojure-lsp" "clean-ns" "--dry"] ;; check if namespaces are clean
                                    "format"       ["clojure-lsp" "format" "--dry"] ;; check if namespaces are formatted
                                    "diagnostics"  ["clojure-lsp" "diagnostics"]
                                    "lint"         ["do" ["clean-ns"] ["format"] ["diagnostics"]]
                                    "clean-ns-fix" ["clojure-lsp" "clean-ns"]
                                    "format-fix"   ["clojure-lsp" "format"]
                                    "lint-fix"     ["do" ["clean-ns-fix"] ["format-fix"]]}
                   :repl-options   {:init-ns common-clj.schema.core}}}

  :resource-paths ["resources"])
