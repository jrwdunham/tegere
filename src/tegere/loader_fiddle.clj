(ns tegere.loader-fiddle
  "Fiddle file for playing around with loader.clj."
  (:require [tegere.loader :as l]
            [tegere.steps :as tegsteps])
  (:import java.io.File))

(defn test-load-clojure-source-files-under-path
  [path]
  (reset! tegsteps/registry {})
  (let [init-reg @tegsteps/registry]
    (l/load-clojure-source-files-under-path path)
    {:before init-reg
     :after @tegsteps/registry}))

(comment

  (->> "examples"
       l/find-clojure-sources-in-dir
       l/get-classpath-additions)  ;; #{"examples/apes/src/"}

  (->> "not/a/local/path"
       l/find-clojure-sources-in-dir
       l/get-classpath-additions)  ;; #{}

  (test-load-clojure-source-files-under-path "examples")

  (test-load-clojure-source-files-under-path "examples/apes")

  (test-load-clojure-source-files-under-path "examples/apes/src")

)
