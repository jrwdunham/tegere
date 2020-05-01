(ns tegere.parser-test
  (:require [tegere.parser :as sut]
            [clojure.test :as t]))

(t/deftest feature-file-semantics-test
  (t/testing "Semantics can convert a real-world .feature file to a Clojure map"
    (let [real-feature
          (slurp (.getPath (clojure.java.io/resource "sample2.feature")))
          feature-map (sut/parse real-feature)
          scenarios (::sut/scenarios feature-map)]
      (t/is (= (::sut/name feature-map) "HTML Email Generation"))
      (t/is (= (::sut/description feature-map)
               (str "Clients of the Document Generation and Distribution APIs"
                    " want to be able to generate HTML email documents using"
                    " the Document Generator Service (DGS).")))
      (t/is (= (::sut/tags feature-map) (list "email-gen")))
      (t/is (= (count scenarios) 17))
      (t/is (= (-> scenarios last ::sut/tags) (list "last-scenario")))
      (t/is (= (-> scenarios last ::sut/steps)
               (list
                {::sut/type :given ::sut/text "x"}
                {::sut/type :when ::sut/text "y"}
                {::sut/type :then ::sut/text "z"}))))))

(t/deftest tag-expressions-parsed-to-query-trees-correctly
  (t/testing "Tag expressions are parsed to query trees correctly"
    (let [expectations
          [["@dog" "dog"]
           ["not @cat" '(not "cat")]
           ["@smoke and @fast" '(and "smoke" "fast")]
           ["@wip and not @slow" '(and "wip" (not "slow"))]
           ["@gui or @database" '(or "gui" "database")]
           ["not @a or @b and not @c or not @d or @e and @f"
            '(or (or (or (not "a") (and "b" (not "c"))) (not "d")) (and "e" "f"))]
           ["not @a or @b" '(or (not "a") "b")]
           ["not @a or @b and not @c" '(or (not "a") (and "b" (not "c")))]
           ["not @a and @b and @c" '(and (and (not "a") "b") "c")]
           ["(not @cat)" '(not "cat")]
           ["(@smoke and @fast)" '(and "smoke" "fast")]
           ["@a and @b and @c" '(and (and "a" "b") "c")]
           ["(@a and @b) and @c" '(and (and "a" "b") "c")]
           ["@a and (@b and @c)" '(and "a" (and "b" "c"))]
           ["(@a and (@b and @c) or @d)" '(or (and "a" (and "b" "c")) "d")]
           ["not @a or @b" '(or (not "a") "b")]
           ;; Old-Style Tag Expressions parsed as fallback
           ["dog" "dog"]
           ["~@dog" '(not "dog")]
           ["cat , ~@dog" '(or "cat" (not "dog"))]
           ["cat , ~@dog,cow     ,   ~bunny"
            '(or "cat" (not "dog") "cow" (not "bunny"))]
           ["cat,~@dog,cow,~bunny" '(or "cat" (not "dog") "cow" (not "bunny"))]]]
      (doseq [[input expectation] expectations]
        (t/is (= (sut/parse-tag-expression-with-fallback input) expectation))))))
