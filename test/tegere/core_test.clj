(ns tegere.core-test
  (:require [clojure.test :refer :all]
            [tegere.core :refer :all]))

(def first-scenario-outline-text
  (str
   "Myrtle wants to generate the Accounts Receivable (AR) HTML email documents"
   " using the DGS and confirm that the generated documents have the expected"
   " properties."))

(deftest feature-file-parse-test
  (testing "Feature parser can parse a real-world .feature file"
    (let [real-feature
          (slurp (.getPath (clojure.java.io/resource "sample.feature")))
          parse (feature-prsr real-feature)
          [root-label & nodes] parse
          first-scenario-outline
          (->> nodes
               (filter (fn [n] (= (first n) :SCENARIO_OUTLINE)))
               first)]
      (do
        (is (= (type parse) clojure.lang.PersistentVector))
        (is (= :FEATURE root-label))
        (is (=
             #{:IGNORED_LINE :SCENARIO_OUTLINE :FEATURE_BLOCK}
             (set (map first nodes))))
        (is (=
             [:FEATURE_BLOCK :SCENARIO_OUTLINE :SCENARIO_OUTLINE]
             (->> nodes
                  (map first)
                  (filter (fn [x] (not= x :IGNORED_LINE)))
              )))
        (is (=
             :SCENARIO_OUTLINE_LINE
             (-> first-scenario-outline (nth 2) first)))
        (is (=
             first-scenario-outline-text
             (-> first-scenario-outline
                 (nth 2)
                 (nth 3)
                 second)))))))
