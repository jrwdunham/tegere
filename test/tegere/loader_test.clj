(ns tegere.loader-test
  "Tests for the loading of feature and steps files."
  (:require [tegere.loader :as sut]
            [clojure.test :as t]))

(t/deftest find-feature-files-test
  (t/testing "We can find feature files under a path"
    (t/is (= (set (map str (sut/find-feature-files "examples/apes")))
             #{"examples/apes/src/apes/features/bonobo-behaviour.feature"
               "examples/apes/src/apes/features/chimpanzee-behaviour.feature"}))))
