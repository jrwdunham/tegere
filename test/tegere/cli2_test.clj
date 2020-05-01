(ns tegere.cli2-test
  (:require [clojure.test :as t]
            [tegere.cli2 :as sut]
            [tegere.loader :as l]
            [tegere.query :as q]
            [tegere.utils :as u]))

(t/deftest parsing-tags-to-query-trees
  (t/testing "We can parse command-line tags options to :tegere.query/query-tree"
    (let [expected '(and (not "ant")
                         (and "rat"
                              (or "cat" (not "dog") "cow" (not "bunny"))))
          actual (-> (sut/validate-args
                      ["examples/apes/src/apes/features"
                       "--tags=cat,~@dog,cow,~bunny"
                       "--tags=rat"
                       "--tags=~@ant"])
                     :tegere.query/query-tree)]
      (t/is (= expected actual)))))

(t/deftest parsed-command-line-tags-query-correctly
  (t/testing "That queries from parsed command-line tags work"
    (let [features (l/load-feature-files "examples/apes/src/apes/features")
          query-tree (-> (sut/validate-args
                          ["examples/apes/src/apes/features"
                           "--tags=bonobos,@chimpanzees"
                           "--tags=orangutan,~@fruit=banana"])
                         :tegere.query/query-tree)
          actual (->> (q/query features query-tree)
                      u/extract-all-scenario-tags
                      (map set))
          expected [#{"fruit=pear"
                      "chimpanzees"
                      "fruit-reactions"
                      "response=sad"
                      "manner_of_looking=loathingly"}
                    #{"chimpanzees"
                      "orangutan"}
                    #{"fruit=pear"
                      "fruit-reactions"
                      "bonobos"
                      "manner_of_looking=indifferently"
                      "response=indifferent"}
                    #{"bonobos"
                      "orangutan"}]]
      (t/is (= expected actual)))))
