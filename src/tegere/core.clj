(ns tegere.core
  "TeGere! means Behave! It is a Gherkin testing library modeled after Python's
  Behave! Are the exclamation marks really necessary? I don't know!"
  (:require [tegere.cli :refer [simple-cli-parser]]
            [tegere.loader :refer [find-feature-files load-steps]]))

(defn main
  [args]
  (println "TeGere!")
  (let [cli-args (simple-cli-parser args)
        target-path (-> cli-args :args first (or "."))
        feature-files (find-feature-files target-path)
        steps (load-steps target-path)
        ]
    {:cli-args cli-args
     :target-path target-path
     :feature-files feature-files
     :steps steps
    }
  )
)

(defn -main
  "Usage:

      $ lein run [& args]

  E.g.:

      $ lein run --tags=monkey --stop
  "
  [& args]
  (main args))
