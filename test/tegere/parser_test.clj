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
