(defproject net.clojars.macielti/common-clj "43.74.76"

  :description "Just common Clojure code that I use across projects"

  :url "https://github.com/macielti/common-clj"

  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :exclusions [log4j]

  :dependencies [[org.clojure/clojure "1.12.4"]
                 [prismatic/schema-generators "0.1.5"]
                 [siili/humanize "0.1.1"]
                 [camel-snake-kebab "0.4.3"]
                 [prismatic/schema "1.4.1"]
                 [buddy/buddy-sign "3.6.1-359"]
                 [cheshire "6.1.0"]
                 [metosin/schema-tools "0.13.1"]
                 [clojure.java-time "1.4.3"]
                 [clj-rate-limiter "0.1.6-RC1"]
                 [dev.weavejester/medley "1.9.0"]
                 [hara/io.scheduler "3.0.12"]
                 [integrant "1.0.1"]
                 [diehard "0.12.0"]
                 [org.clojure/tools.logging "1.3.0"]
                 [danlentz/clj-uuid "0.2.0"]
                 [commons-io/commons-io "2.21.0"]]

  :profiles {:dev {:resource-paths ^:replace ["test/resources"]

                   :test-paths     ^:replace ["test/unit" "test/integration" "test/helpers"]

                   :plugins        [[lein-cloverage "1.2.4"]
                                    [com.github.clojure-lsp/lein-clojure-lsp "2.0.13"]
                                    [com.github.liquidz/antq "RELEASE"]]

                   :dependencies   [[net.clojars.macielti/common-test-clj "5.2.4"]
                                    [org.slf4j/slf4j-api "2.0.17"]
                                    [ch.qos.logback/logback-classic "1.5.23"]
                                    [net.clojars.macielti/service-component "5.4.2"]
                                    [nubank/matcher-combinators "3.9.2"]
                                    [clj-http-fake "1.0.4"]
                                    [nubank/mockfn "0.7.0"]
                                    [ring "1.12.2"]
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
