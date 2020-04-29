(ns tegere.fiddle.query
  (:require [tegere.loader :as l]
            [tegere.query :as q]))

(comment

  (q/get-all-scenario-tags
   (l/load-feature-files "examples/apes/src/apes/features"))

  (q/set-all-scenario-tags
   (l/load-feature-files "examples/apes/src/apes/features"))

)
