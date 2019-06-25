(ns tegere.loader-fiddle
  "Fiddle file for playing around with loader.clj."
  (:require [clojure.string :as s]
            [tegere.loader :refer :all]))


(comment

  (let [x ["/path/"]]
    (-> x
        first
        (or ".")
        ))

  (find-feature-files "examples/apes")

  (find-clojure-files "examples/apes")

  (load-steps "examples/apes")

  (->> (load-steps "examples/apes")
       vals
       (map keys)
       flatten
       set)

  (let [r (load-steps "examples/apes")
        f1 (-> r :given (get "a setup"))
        f2 (-> r :when (get "an action"))
        f3 (-> r :then (get "a result"))]
    [(f1 {}) (f2 {}) (f3 {})])

)
