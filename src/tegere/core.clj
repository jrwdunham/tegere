(ns tegere.core
  "TeGere! means Behave! It is a Gherkin testing library modeled after Python's
  Behave! Are the exclamation marks really necessary? I don't know!"
  (:gen-class)
  (:require [tegere.cli :refer [simple-cli-parser]]
            [tegere.loader :refer [load-feature-files
                                   load-clojure-source-files-under-path]]
            [tegere.runner :refer [run]]
            [tegere.steps :as tegsteps]))

(defn main
  [args]
  (let [cli-args (simple-cli-parser args)
        config {:tags (select-keys (:kwargs cli-args) [:and-tags :or-tags])
                :stop (get-in cli-args [:kwargs :stop] false)}
        target-path (-> cli-args :args first (or "."))
        features (load-feature-files target-path)]
    (load-clojure-source-files-under-path target-path)
    (run features @tegsteps/registry config)))

(defn -main
  "Usage:

      $ lein run [& args]

  E.g.:

      $ lein run --tags=monkey --stop
  "
  [& args]
  (main args))
