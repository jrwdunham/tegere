(defproject tegere "0.1.0-SNAPSHOT"
  :description "A Gherkin library for Clojure. The name 'te gere' is intended to be a Latin translation of English 'behave!'. Modeled after the Python Behave library."
  :url "https://github.com/jrwdunham/tegere"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[alandipert/intension "1.1.1"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/spec.alpha "0.2.176"]
                 [org.clojure/tools.cli "1.0.194"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [instaparse "1.4.10"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
