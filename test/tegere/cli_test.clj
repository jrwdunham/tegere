(ns tegere.cli-test
  (:require [clojure.test :as t]
            [tegere.cli :as sut]
            [tegere.loader :as l]
            [tegere.query :as q]
            [tegere.utils :as u]))

(t/deftest cli-arg-opt-parsing-works
  (t/testing "That parsing of command-line arguments and options works"
    (let [input
          ["examples/apes/src/apes/features"
           "--tags=cow,chicken"
           "--tags=not @a or @b and not @c or not @d or @e and @f"
           "-Durl=http://api.example.com"
           "--data=password=secret"
           "--stop"
           "--verbose"]
          expected
          {:tegere.runner/stop true
           :tegere.runner/verbose true
           :tegere.runner/data {:url "http://api.example.com"
                                :password "secret"}
           :tegere.query/query-tree
           '(and
             (or (or (or (not "a") (and "b" (not "c"))) (not "d")) (and "e" "f"))
             (or "cow" "chicken")),
           :tegere.runner/features-path "examples/apes/src/apes/features"}]
      (t/is (= expected (sut/validate-args input))))))

(t/deftest parsing-tags-to-query-trees
  (t/testing "We can parse command-line tags options to :tegere.query/query-tree"
    (let [expectations
          [['(and (not "ant")
                  (and "rat"
                       (or "cat" (not "dog") "cow" (not "bunny"))))
            ["--tags=cat,~@dog,cow,~bunny"
             "--tags=rat"
             "--tags=~@ant"]]
           ['(and
              (or (or (or (not "a") (and "b" (not "c"))) (not "d")) (and "e" "f"))
              (or "cow" "chicken"))
            ["--tags=cow,chicken"
             "--tags=not @a or @b and not @c or not @d or @e and @f"]]]]
      (doseq [[expected input] expectations]
        (let [actual
              (-> (sut/validate-args
                   (vec (concat ["examples/apes/src/apes/features"] input)))
                  :tegere.query/query-tree)]
          (t/is (= expected actual)))))))

(t/deftest parsed-command-line-tags-query-correctly
  (t/testing "CHANGES That queries from parsed command-line tags work"
    (let [features (l/load-feature-files "examples/apes/src/apes/features")
          expectations
          [[["--tags=bonobos,@chimpanzees"
             "--tags=orangutan,~@fruit=banana"]
            #{#{"fruit=pear"
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
                "orangutan"}}]
           [["--tags=((@fruit=banana and @chimpanzees) or ((not @fruit=banana) and @bonobos))"]
            #{#{"manner_of_looking=quizzically"
                "chimpanzees"
                "fruit-reactions"
                "response=happy"
                "fruit=banana"}
              #{"fruit=pear"
                "fruit-reactions"
                "bonobos"
                "manner_of_looking=indifferently"
                "response=indifferent"}
              #{"bonobos"
                "orangutan"}}]]]
      (doseq [[tags expected] expectations]
        (let [query-tree
              (-> (sut/validate-args
                   (vec (concat ["examples/apes/src/apes/features"] tags)))
                  :tegere.query/query-tree)
              actual (->> (q/query features query-tree)
                          u/extract-all-scenario-tags
                          (map set)
                          set)]
          (t/is (= expected actual)))))))

