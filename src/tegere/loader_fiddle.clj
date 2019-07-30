(ns tegere.loader-fiddle
  "Fiddle file for playing around with loader.clj."
  (:require [tegere.loader :refer :all]
            [tegere.steps :as tegsteps]
            [clojure.string :as s])
  (:import java.io.File))

(defn test-load-clojure-source-files-under-path
  [path]
  (reset! tegsteps/registry {})
  (let [init-reg @tegsteps/registry]
    (load-clojure-source-files-under-path path)
    {:before init-reg
     :after @tegsteps/registry}))

(comment

  (->> "examples"
       find-clojure-sources-in-dir
       get-classpath-additions)  ;; #{"examples/apes/src/"}

  (->> "not/a/local/path"
       find-clojure-sources-in-dir
       get-classpath-additions)  ;; #{}

  (test-load-clojure-source-files-under-path "examples")

  (test-load-clojure-source-files-under-path "examples/apes")

  (test-load-clojure-source-files-under-path "examples/apes/src")

)
