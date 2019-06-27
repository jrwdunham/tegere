(ns tegere.loader-test
  "Tests for the loading of feature and steps files."
  (:require [tegere.loader :as sut]
            [tegere.steps :as tegsteps]
            [clojure.test :as t]))

(t/deftest find-feature-files-test
  (t/testing "We can find feature files under a path"
    (t/is (= (map str (sut/find-feature-files "examples/apes"))
             (list
              "examples/apes/features/bonobo-behaviour.feature"
              "examples/apes/features/monkey-behaviour.feature")))))

(t/deftest load-clojure-source-files-under-path-test
  (t/testing "We can load Clojure steps files and extract their registry maps"
    (reset! tegsteps/registry {})
    (sut/load-clojure-source-files-under-path "examples/apes")
    (let [loaded-registry @tegsteps/registry
          registry-keys (set (keys loaded-registry))
          step-fn-texts (->> loaded-registry
                             vals (map keys) flatten set)
          a-given (-> loaded-registry :given (get "a {animal}"))
          a-when (-> loaded-registry :when (get "I give him a {noun}"))
          a-then (-> loaded-registry :then (get "he is {adjective}"))]
    (do
        (t/is (= #{:given :when :then} registry-keys))
        (t/is (= #{"a {animal}"
                   "everything is all good"
                   "a result"
                   "he doesn't eat it"
                   "he is {adjective}"
                   "he looks at me {adverb}"
                   "an action"
                   "I give him a {noun}"
                   "I present him with an orangutan"}
                 step-fn-texts))
        (t/is (= [:a-monkey :give-him-a-banana :is-happy]
                 (-> {}
                     (a-given "monkey")
                     (a-when "banana")
                     (a-then "happy")
                     :step-rets)))))))
