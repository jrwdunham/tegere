(ns tegere.fiddle.loader
  "Fiddle file for playing around with loader.clj."
  (:require [tegere.loader :as loader]))

(comment

  ;; Load the feature files in the example Apes project into a maybe
  ;; `::tegere.parser/features` collection.
  (loader/load-feature-files "examples/apes/src/apes/features")

)
