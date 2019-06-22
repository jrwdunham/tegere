(ns tegere.runner-fiddle
  "Fiddle file for playing around with runner.clj."
  (:require [clojure.string :as s]
            [clojure.set :refer [intersection]]
            [tegere.runner :refer :all]
            [tegere.parser :refer [parse]]
            [tegere.grammar-fiddle :refer [monkey-feature]]))


(defn a
  [features]
  [(cons :a features) nil])

(defn b
  [features]
  [(cons :b features) nil])

(defn c
  [features]
  [nil "i dont like this!"])

(comment

  (let [features [(parse monkey-feature) (parse monkey-feature)]
        tags {:and-tags #{"monkeys" "fruit-reactions"}}
        fake-registry {:given {"a monkey" (fn [a] 2)}}]
    (run features tags fake-registry))

  (err->> (list 1 2 3)
          a
          b
          c
          )

)
