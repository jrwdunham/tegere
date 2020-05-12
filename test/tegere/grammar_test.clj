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

(t/deftest step-block-parse-test
  (t/testing
      (str "Step blocks are parsed correctly, even when steps contain linebreaks"
           " and/or step data")
    (let [step-block
          (str
           "    Given an Inspector Spacetime\nwith attributes\n"
           "      | cool?   | loquacious? | precocious? |\n"
           "      | mos def | nope        | indubitably |\n"
           "    When he travels throught time\n"
           "    Then he does not travel through space\n")
          step-block-parse (sut/step-block-prsr step-block)
          so-step-block
          (str
           "    Given a <x> synchronized\n"
           "      between Mufu and Bugu\n"
           "      | blargon_id | parent_id | owner_id | environment |\n"
           "      | 3170       | 651       | 13       | local       |\n"
           "    When the blargon is pinched in Bugu\n"
           "    Then he yelps in Mufu\n"
           "\n")
          so-step-block-parse (sut/so-step-block-prsr so-step-block)]
      (t/is (= step-block-parse
               [:STEP_BLOCK
                [:STEP
                 [:STEP_LABEL [:GIVEN_LABEL "Given"]]
                 [:STEP_TEXT " an Inspector Spacetime" "with attributes"]
                 [:STEP_DATA
                  [:STEP_DATA_ROW
                   [:STEP_DATA_CELL " cool?   "]
                   [:STEP_DATA_CELL " loquacious? "]
                   [:STEP_DATA_CELL " precocious? "]]
                  [:STEP_DATA_ROW
                   [:STEP_DATA_CELL " mos def "]
                   [:STEP_DATA_CELL " nope        "]
                   [:STEP_DATA_CELL " indubitably "]]]]
                [:STEP
                 [:STEP_LABEL [:WHEN_LABEL "When"]]
                 [:STEP_TEXT " he travels throught time"]]
                [:STEP
                 [:STEP_LABEL [:THEN_LABEL "Then"]]
                 [:STEP_TEXT " he does not travel through space"]]]))
      (t/is (= so-step-block-parse
               [:SO_STEP_BLOCK
                [:SO_STEP
                 [:STEP_LABEL [:GIVEN_LABEL "Given"]]
                 [:SO_STEP_TEXT
                  [:SO_STEP_STR " a "]
                  [:VARIABLE "x"]
                  [:SO_STEP_STR " synchronized"]
                  [:SO_STEP_STR "      between Mufu and Bugu"]]
                 [:STEP_DATA
                  [:STEP_DATA_ROW
                   [:STEP_DATA_CELL " blargon_id "]
                   [:STEP_DATA_CELL " parent_id "]
                   [:STEP_DATA_CELL " owner_id "]
                   [:STEP_DATA_CELL " environment "]]
                  [:STEP_DATA_ROW
                   [:STEP_DATA_CELL " 3170       "]
                   [:STEP_DATA_CELL " 651       "]
                   [:STEP_DATA_CELL " 13       "]
                   [:STEP_DATA_CELL " local       "]]]]
                [:SO_STEP
                 [:STEP_LABEL [:WHEN_LABEL "When"]]
                 [:SO_STEP_TEXT [:SO_STEP_STR " the blargon is pinched in Bugu"]]]
                [:SO_STEP
                 [:STEP_LABEL [:THEN_LABEL "Then"]]
                 [:SO_STEP_TEXT [:SO_STEP_STR " he yelps in Mufu"]]]
                [:IGNORED_LINE [:EMPTY_LINE "\n"]]])))))
