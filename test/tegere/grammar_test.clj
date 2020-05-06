(ns tegere.grammar-test
  (:require [tegere.grammar :as sut]
            [clojure.test :as t]))

(def first-scenario-outline-text
  (str
   "Myrtle wants to generate the Accounts Receivable (AR) HTML email documents"
   " using the DGS and confirm that the generated documents have the expected"
   " properties."))

(t/deftest feature-file-parse-test
  (t/testing "Feature parser can parse a real-world .feature file"
    (let [real-feature
          (slurp (.getPath (clojure.java.io/resource "sample.feature")))
          parse (sut/feature-prsr real-feature)
          [root-label & nodes] parse
          first-scenario-outline
          (->> nodes
               (filter (fn [n] (= (first n) :SCENARIO_OUTLINE)))
               first)]
      (t/is (= (type parse) clojure.lang.PersistentVector))
      (t/is (= :FEATURE root-label))
      (t/is (=
             #{:IGNORED_LINE :SCENARIO_OUTLINE :FEATURE_BLOCK}
             (set (map first nodes))))
      (t/is (=
             [:FEATURE_BLOCK
              :SCENARIO_OUTLINE
              :SCENARIO_OUTLINE
              :SCENARIO_OUTLINE
              :SCENARIO_OUTLINE]
             (->> nodes
                  (map first)
                  (filter (fn [x] (not= x :IGNORED_LINE))))))
      (t/is (=
             :SCENARIO_OUTLINE_LINE
             (-> first-scenario-outline (nth 2) first)))
      (t/is (=
             first-scenario-outline-text
             (-> first-scenario-outline
                 (nth 2)
                 (nth 3)
                 second))))))
