(ns tegere.parser-test
  (:require [tegere.parser :as sut]
            [clojure.test :as t]))

(t/deftest feature-file-semantics-test
  (t/testing "Semantics can convert a real-world .feature file to a Clojure map"
    (let [real-feature
          (slurp (.getPath (clojure.java.io/resource "sample2.feature")))
          feature-map (sut/parse real-feature)
          scenarios (:scenarios feature-map)]
      (do
        (t/is (= (:name feature-map) "HTML Email Generation"))
        (t/is (= (:description feature-map)
                 (str "Clients of the Document Generation and Distribution APIs"
                      " want to be able to generate HTML email documents using"
                      " the Document Generator Service (DGS).")))
        (t/is (= (:tags feature-map) (list "email-gen")))
        (t/is (= (count scenarios) 17))
        (t/is (= (-> scenarios last :tags) (list "last-scenario")))
        (t/is (= (-> scenarios last :steps)
                 (list
                  {:type "Given" :text "x"}
                  {:type "When" :text "y"}
                  {:type "Then" :text "z"})))))))
