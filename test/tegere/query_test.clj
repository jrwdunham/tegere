(ns tegere.query-test
  (:require [clojure.test :as t]
            [tegere.loader :as l]
            [tegere.parser :as p]
            [tegere.query :as sut]
            [tegere.utils :as u]))

(t/deftest old-style-query-expressions-work
  (t/testing (str "That old-style query expressions return the same values as"
                  " explicit query trees")
    (let [features (l/load-feature-files "examples/apes/src/apes/features")
          where-sets
          [["bonobos"
            (p/parse-old-style-tag-expression "@bonobos")
            (p/parse-old-style-tag-expression "bonobos")]
           ['(not "bonobos")
            (p/parse-old-style-tag-expression "~@bonobos")
            (p/parse-old-style-tag-expression "~bonobos")]
           ['(or "fruit=banana" (not "chimpanzees"))
            (p/parse-old-style-tag-expression "fruit=banana,~@chimpanzees")]]]
      (apply
       =
       (for [ws where-sets]
         (apply
          =
          (for [where ws]
            (->> (sut/query features where) u/extract-all-scenario-tags))))))))
