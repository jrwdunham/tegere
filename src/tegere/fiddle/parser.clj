(ns tegere.fiddle.parser
  "Fiddle file for playing around with parser.clj."
  (:require [tegere.parser :as parser]
            [clojure.java.io :as io]))

(comment

  (let [real-feature
        (slurp (.getPath (io/resource "sample2.feature")))]
    (parser/parse real-feature))

  (parser/parse-tag-expression-with-fallback "@dog,cat or @fish")

  (parser/parse-tag-expression "dog")

  (parser/parse-tag-expression
   "not @a or @b and not @c or not @d or @e and @f")

  (parser/parse-tag-expression-with-fallback
   "not @a or @b")

)
