(ns tegere.runner-fiddle
  "Fiddle file for playing around with runner.clj."
  (:require [clojure.string :as s]
            [tegere.runner :refer :all]
            [tegere.parser :refer [parse]]
            [tegere.grammar-fiddle :refer [monkey-feature]]))


(comment

  (let [features [(parse monkey-feature)]
        tags {:and ["monkeys"]
              :or []}]
    (run features tags)
    )

  (seq [])

  (some #{:z} [:a :b])

)
