(defproject net.clojars.macielti/common-clj "40.72.72"

  :description "Just common Clojure code that I use across projects"

  :url "https://github.com/macielti/common-clj"

  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :exclusions [log4j]

  :dependencies [[org.clojure/clojure "1.12.0"]
                 [prismatic/schema-generators "0.1.5"]
                 [siili/humanize "0.1.1"]
                 [camel-snake-kebab "0.4.3"]
                 [prismatic/schema "1.4.1"]
                 [buddy/buddy-sign "3.6.1-359"]
                 [cheshire "5.13.0"]
                 [metosin/schema-tools "0.13.1"]
                 [clj-commons/iapetos "0.1.14"]
                 [clojure.java-time "1.4.3"]
                 [clj-rate-limiter "0.1.6-RC1"]
                 [dev.weavejester/medley "1.8.1"]
                 [hara/io.scheduler "3.0.12"]
                 [integrant "0.13.1"]
                 [amazonica "0.3.167"]
                 [diehard "0.11.12"]
                 [overtone/at-at "1.4.65"]
                 [commons-io/commons-io "2.17.0"]
                 [org.clojure/tools.logging "1.3.0"]
                 [org.clojure/tools.reader "1.5.0"]]

  :profiles {:dev {:resource-paths ^:replace ["test/resources"]

                   :test-paths     ^:replace ["test/unit" "test/integration" "test/helpers"]

                   :plugins        [[lein-cloverage "1.2.4"]
                                    [com.github.clojure-lsp/lein-clojure-lsp "1.4.15"]
                                    [com.github.liquidz/antq "RELEASE"]]

                   :dependencies   [[net.clojars.macielti/common-test-clj "1.1.1"]
                                    [org.slf4j/slf4j-api "2.0.16"]
                                    [ch.qos.logback/logback-classic "1.5.12"]
                                    [net.clojars.macielti/service-component "2.4.2"]
                                    [nubank/matcher-combinators "3.9.1"]
                                    [clj-http-fake "1.0.4"]
                                    [nubank/mockfn "0.7.0"]
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
