(ns tegere.fiddle.query
  (:require [tegere.loader :as l]
            [tegere.query :as query]))

(comment

  ;; Experiment with pasing ``:tegere.query/query-tree`` expressions to
  ;; ``:tegere.query/query`` here. The output should be a list of all-tags
  ;; vectors. Each vector is the set of tags belonging to a scenario that maches
  ;; the supplied query.
  (let [features (l/load-feature-files "examples/apes/src/apes/features")
        extract-all-scenario-tags
        (fn [features]
          (mapcat (fn [f]
                    (->> f
                         :tegere.parser/scenarios
                         (map :tegere.query/tags)))
                  features))
        where
        '(and
          (or "chimpanzees" "orangutan")
          (not "response=sad"))]
    (->> (query/query features where)
         extract-all-scenario-tags))

)
