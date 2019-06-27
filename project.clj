(defproject tegere "0.1.0-SNAPSHOT"
  :description "A Gherkin library for Clojure. The name 'te gere' is intended to be a Latin translation of English 'behave!'. Modeled after the Python Behave library."
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [com.cemerick/pomegranate "1.1.0"]
                 [me.raynes/fs "1.4.6"]
                 [instaparse "1.4.10"]]
  :main ^:skip-aot tegere.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :repl-options {:init-ns tegere.core})
