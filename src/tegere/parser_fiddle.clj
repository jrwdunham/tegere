(ns tegere.parser-fiddle
  "Fiddle file for playing around with parser.clj."
  (:require [clojure.string :as s]
            [tegere.parser :refer :all]))


(comment

  (let [real-feature
        (slurp (.getPath (clojure.java.io/resource "sample2.feature")))]
    (parse real-feature))

)
