(ns tegere.loader-test
  "Tests for the loading of feature and steps files."
  (:require [tegere.loader :as sut]
            [clojure.test :as t]))

(t/deftest find-feature-files-test
  (t/testing "We can find feature files under a path"
    (t/is (= (map str (sut/find-feature-files "examples/apes"))
             (list
              "examples/apes/features/bonobo-behaviour.feature"
              "examples/apes/features/monkey-behaviour.feature")))))

(t/deftest find-clojure-files-test
  (t/testing "We can find Clojure source files under a path"
    (t/is (= (map str (sut/find-clojure-files "examples/apes"))
             (list
              "examples/apes/steps/monkeys.clj"
              "examples/apes/steps/bonobos.clj"
              "examples/apes/steps/orangutans.clj")))))

(t/deftest load-steps-files-test
  (t/testing "We can load Clojure steps files and extract their registry maps"
    (let [loaded-registry (sut/load-steps "examples/apes")
          registry-keys (set (keys loaded-registry))
          step-fn-texts (->> loaded-registry
                             vals (map keys) flatten set)
          fs [(-> loaded-registry :given (get "a setup"))
              (-> loaded-registry :when (get "an action"))
              (-> loaded-registry :then (get "a result"))]]
      (do
        (t/is (= #{:given :when :then} registry-keys))
        (t/is (= #{"an action" "a setup" "a result"} step-fn-texts))
        (t/is (= [:an-orangutan-setup :an-action :a-result]
                 (map (fn [f] (f {})) fs)))))))
