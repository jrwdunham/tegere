(ns tegere.fiddle.parser
  "Fiddle file for playing around with parser.clj."
  (:require [tegere.parser :as parser]
            [clojure.java.io :as io]))

(comment

  (let [real-feature
        (slurp (.getPath (io/resource "sample2.feature")))]
    (parser/parse real-feature))

)
