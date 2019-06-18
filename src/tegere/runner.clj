(ns tegere.runner
  (:require [tegere.parser :refer [parse]]))

(defn get-features-matching-all
  [features tags]
  :features-matching-all
  )

(defn get-features-matching-any
  [features tags]
  :features-matching-any
)

(defn get-features-to-run
  [features tags]
  (cond
    (seq (:and tags))
    (get-features-matching-all features (:and tags))
    (seq (:or tags))
    (get-features-matching-any features (:or tags))
    :else
    features))

(defn run
  [features tags]
  (let [features-to-run (get-features-to-run features tags)]
    features-to-run)
)
