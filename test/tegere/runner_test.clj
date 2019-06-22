(ns tegere.runner-test
  (:require [clojure.test :as t]
            [tegere.runner :as sut]
            [tegere.parser :refer [parse]]))

(def monkey-feature
  (str
   "# This is a comment about this feature\n"
   "\n"
   "@monkeys\n"
   "Feature: Monkeys behave as expected\n"
   "  Experimenters want to ensure that their monkey simulations are behaving\n"
   "  corectly.\n"
   "\n"
   "\n"
   "  # This is a comment about this scenario ...\n"
   "  @fruit-reactions\n"
   "  Scenario Outline: Monkeys behave as expected when offered various foods.\n"
   "    Given a monkey\n"
   "    When I give him a <fruit>\n"
   "    Then he is <response>\n"
   "    But he doesn't eat it\n"
   "    And he looks at me <manner_of_looking>\n"
   "   \n"
   "   \n"
   "  Examples: monkey characteristics:\n"
   "  | fruit  | response  | manner_of_looking  |\n"
   "  | banana | happy     | quizically         |\n"
   "  | pear   | sad       | loathingly         |\n"
   "   \n"
   "  # This is a comment about this scenario outline...\n"
   "\n"
   "  @orangutan\n"
   "  Scenario: Monkeys adore orangutans.\n"
   "    Given a monkey\n"
   "    When I present him with an orangutan\n"
   "    Then he is happy\n"
   "\n"
   "\n"))

(defn count-scenarios
  [features]
  (reduce +
          (map (fn [feat] (-> feat :scenarios count)) features)))

(t/deftest all-tags-filtering-test
  (t/testing "All/and tag filters work"
    (let [features [(parse monkey-feature)]
          scenario-count (count-scenarios features)
          get-match-count (fn [expr]
                            (count-scenarios
                             (sut/get-features-to-run
                              features expr)))]
      (do
        (t/is (= 3 scenario-count))
        (t/is (= 3 (get-match-count
                    {:and-tags #{"monkeys"}})))
        (t/is (= 1 (get-match-count
                    {:and-tags #{"monkeys" "orangutan"}})))
        (t/is (= 1 (get-match-count
                    {:and-tags #{"orangutan"}})))
        (t/is (= 2 (get-match-count
                    {:and-tags #{"monkeys" "fruit-reactions"}})))
        (t/is (= 2 (get-match-count
                    {:and-tags #{"fruit-reactions"}})))
        (t/is (= 0 (get-match-count
                    {:and-tags #{"orangutan" "fruit-reactions"}})))
        (t/is (= 0 (get-match-count
                    {:and-tags #{"fake-tag"}})))))))

(t/deftest any-tags-filtering-test
  (t/testing "Any/or tag filters work"
    (let [features [(parse monkey-feature)]
          scenario-count (count-scenarios features)
          get-match-count (fn [expr]
                            (count-scenarios
                             (sut/get-features-to-run
                              features expr)))]
      (do
        (t/is (= 3 (get-match-count
                    {:or-tags #{"monkeys"}})))
        (t/is (= 3 (get-match-count
                    {:or-tags #{"monkeys" "orangutan"}})))
        (t/is (= 1 (get-match-count
                    {:or-tags #{"orangutan"}})))
        (t/is (= 3 (get-match-count
                    {:or-tags #{"monkeys" "fruit-reactions"}})))
        (t/is (= 2 (get-match-count
                    {:or-tags #{"fruit-reactions"}})))
        (t/is (= 3 (get-match-count
                    {:or-tags #{"orangutan" "fruit-reactions"}})))
        (t/is (= 0 (get-match-count
                    {:or-tags #{"fake-tag"}})))))))

(t/deftest all-tags-override-or-tags-test
  (t/testing "All/and tag filters override any/or tag filters"
    (let [features [(parse monkey-feature)]
          scenario-count (count-scenarios features)
          get-match-count (fn [expr]
                            (count-scenarios
                             (sut/get-features-to-run
                              features expr)))]
      (do
        (t/is (= 3 (get-match-count
                    {:and-tags #{"monkeys"}
                     :or-tags #{"monkeys"}})))
        (t/is (= 1 (get-match-count
                    {:and-tags #{"monkeys" "orangutan"}
                     :or-tags #{"monkeys" "orangutan"}})))
        (t/is (= 1 (get-match-count
                    {:and-tags #{"orangutan"}
                     :or-tags #{"orangutan"}})))
        (t/is (= 2 (get-match-count
                    {:and-tags #{"monkeys" "fruit-reactions"}
                     :or-tags #{"monkeys" "fruit-reactions"}})))
        (t/is (= 2 (get-match-count
                    {:and-tags #{"fruit-reactions"}
                     :or-tags #{"fruit-reactions"}})))
        (t/is (= 0 (get-match-count
                    {:and-tags #{"orangutan" "fruit-reactions"}
                     :or-tags #{"orangutan" "fruit-reactions"}})))
        (t/is (= 0 (get-match-count
                    {:and-tags #{"fake-tag"}
                     :or-tags #{"fake-tag"}})))))))
