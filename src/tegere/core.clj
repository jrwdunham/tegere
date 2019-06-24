(ns tegere.core
  "TeGere! means Behave! It is a Gherkin testing library modeled after Python's
  Behave! Are the exclamation marks really necessary? I don't know!"
  (:require [tegere.cli :refer [simple-cli-parser]]))

(defn -main
  "Usage:

      $ lein run [& args]

  E.g.:

      $ lein run --tags=monkey --stop
  "
  [& args]
  (println "TeGere!")
  (println (simple-cli-parser args))
)
