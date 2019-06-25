(ns tegere.core
  "TeGere! means Behave! It is a Gherkin testing library modeled after Python's
  Behave! Are the exclamation marks really necessary? I don't know!"
  (:require [tegere.cli :refer [simple-cli-parser]]
            [tegere.loader :refer [load-feature-files load-steps]]
            [tegere.runner :refer [run]]))

(defn main
  [args]
  (println "TeGere!")
  (let [cli-args (simple-cli-parser args)
        config {:tags (select-keys (:kwargs cli-args) [:and-tags :or-tags])
                :stop (get-in cli-args [:kwargs :stop] false)}
        target-path (-> cli-args :args first (or "."))
        features (load-feature-files target-path)
        registry (load-steps target-path)
        ]
    [{:cli-args cli-args
      :config config
      :target-path target-path
      :features features
      :registry registry
     }
     (run features registry config)]
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
