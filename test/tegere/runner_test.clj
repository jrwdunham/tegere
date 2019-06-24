(ns tegere.runner-test
  (:require [clojure.test :as t]
            [tegere.runner :as sut]
            [tegere.parser :refer [parse]]))

;; A feature file (string) to use for testing
(def monkey-feature
  (str
   "# This is a comment about this feature\n"
   "\n"
   "@monkeys\n"
   "Feature: Monkeys behave as expected\n"
   "  Experimenters want to ensure that their monkey simulations are behaving\n"
   "  correctly.\n"
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

(defn update-step-rets
  "Convenience testing function that appends val to the :step-rets key of the
  map context, while ensuring that the val of :step-rets is a vec."
  [context val]
  (update-in
   context
   [:step-rets]
   (fn [step-rets]
     (if (seq step-rets)
       (conj step-rets val)
       [val]))))

;; A fake registry of step functions to test our Monkey Feature
(def fake-registry
  {:given {"a monkey" (fn [context] (update-step-rets context :a-monkey))}
   :when {"I give him a banana"
          (fn [context] (update-step-rets context :give-banana))
          "I give him a pear"
          (fn [context] (update-step-rets context :give-pear))
          "I present him with an orangutan"
          (fn [context] (update-step-rets context :present-with-orang))}
   :then {"he doesn't eat it" (fn [context] (update-step-rets context :not-eat))
          "he is happy" (fn [context] (update-step-rets context :is-happy))
          "he is sad" (fn [context] (update-step-rets context :is-sad))
          "he looks at me loathingly"
          (fn [context] (update-step-rets context :looks-loathingly))
          "he looks at me quizically"
          (fn [context] (update-step-rets context :looks-quizically))}})

;; This fake registry has a step function that takes parameters
(def fake-registry-params
  (assoc-in
   fake-registry
   [:when "I give him a {fruit}"]
   (fn [context fruit]
     (update-step-rets context (keyword (format "give-with-var-%s" fruit))))))

;; This fake registry has a step function will raise an exception
(def fake-registry-error
  (assoc-in
   fake-registry
   [:then "he is happy"]
   (fn [context] (update-step-rets context (/ 1 0)))))

(defn count-scenarios
  "Return the number of scenarios in seq features."
  [[features err]]
  (if err
    0
    (reduce +
            (map (fn [feat] (-> feat :scenarios count)) features))))

(t/deftest all-tags-filtering-test
  (t/testing "All/and tag filters work"
    (let [features [(parse monkey-feature)]
          scenario-count (count-scenarios [features nil])
          get-match-count (fn [expr]
                            (count-scenarios
                             (sut/get-features-to-run
                              expr features)))]
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
          scenario-count (count-scenarios [features nil])
          get-match-count (fn [expr]
                            (count-scenarios
                             (sut/get-features-to-run
                              expr features)))]
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
          scenario-count (count-scenarios [features nil])
          get-match-count (fn [expr]
                            (count-scenarios
                             (sut/get-features-to-run
                              expr features)))]
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

(t/deftest can-run-simple-feature-test
  (t/testing "We can run a simple feature"
    (let [features [(parse monkey-feature)]
          execution (sut/run features fake-registry {})
          exec-steps (->> execution (map :steps) flatten)
          exec-step-count (count exec-steps)
          success-exec-steps (filter
                              (fn [s] (nil? (-> s :execution :err))) exec-steps)
          success-exec-step-count (count success-exec-steps)
          exec-step-rets
          (map (fn [s]
                 (get-in s [:execution :ctx-after-exec :step-rets])) exec-steps)]
      (do
        ;; Feature with 1 2-line Scenario Outline and 1 Scenario should result
        ;; in 3 step-maps in the execution:
        (t/is (= 3 (count execution)))
        ;; The 2 Scenario Outlines have 5 steps each and the sole Scenario has
        ;; 3 steps; expect 13 steps in the execution:
        (t/is (= 13 exec-step-count))
        ;; All 13 step executions should have been successful
        (t/is (= exec-step-count success-exec-step-count))
        ;; The contexts at the end of execution of each scenario shoulld reflect
        ;; the meaning of the scenario (because the fake registry is defined that
        ;; way.)
        (t/is (= [[:a-monkey :give-banana :is-happy :not-eat :looks-quizically]
                  [:a-monkey :give-pear :is-sad :not-eat :looks-loathingly]
                  [:a-monkey :present-with-orang :is-happy]]
                 [(nth exec-step-rets 4) (nth exec-step-rets 9)
                  (nth exec-step-rets 12)]))))))

(t/deftest step-fns-can-take-args-test
  (t/testing "step functions can take arguments"
    (do
      ;; Match with 2 args
      (t/is (= (sut/get-step-fn-args "I give {recipient} a {fruit}"
                                     "I give him a big ole banana")
               (list "him" "big ole banana")))
      ;; No match
      (t/is (= (sut/get-step-fn-args "I gave {recipient} a {fruit}"
                                     "I give him a big ole banana")
               nil))
      ;; Match with 0 args
      (t/is (= (sut/get-step-fn-args "I give him a big ole banana"
                                     "I give him a big ole banana")
               ())))))

(t/deftest step-functions-that-take-params-test
  (t/testing "We can run a simple feature against a steps registry with steps
             that take parameters"
    (let [features [(parse monkey-feature)]
          execution (sut/run features fake-registry-error {})
          exec-steps (->> execution (map :steps) flatten)
          exec-step-count (count exec-steps)
          success-exec-steps (filter
                              (fn [s] (nil? (-> s :execution :err))) exec-steps)
          success-exec-step-count (count success-exec-steps)
          exec-step-rets
          (map (fn [s]
                 (get-in s [:execution :ctx-after-exec :step-rets])) exec-steps)]
      (do
        ;; Feature with 1 2-line Scenario Outline and 1 Scenario should result
        ;; in 3 step-maps in the execution:
        (t/is (= 3 (count execution)))
        ;; The 2 Scenario Outlines have 5 steps each and the sole Scenario has
        ;; 3 steps; expect 13 steps in the execution:
        (t/is (= 13 exec-step-count))
        ;; The 2 "Then he is happy" steps fail because of the error registry.
        (t/is (= 11 success-exec-step-count))
        ;; The contexts at the end of execution of each scenario shoulld reflect
        ;; the meaning of the scenario (because the fake registry is defined that
        ;; way.)
        (t/is (= [nil [:a-monkey :give-pear :is-sad :not-eat :looks-loathingly]
                  nil]
                 [(nth exec-step-rets 4) (nth exec-step-rets 9)
                  (nth exec-step-rets 12)]))))))

(t/deftest stop-flag-works-test
  (t/testing "The :stop true flag tells the test runner to stop running
             scenarios after the first one fails."
    (let [features [(parse monkey-feature)]
          execution (sut/run features fake-registry-error {:stop true})
          exec-steps (->> execution (map :steps) flatten)
          exec-step-count (count exec-steps)
          success-exec-steps (filter
                              (fn [s] (nil? (-> s :execution :err))) exec-steps)
          success-exec-step-count (count success-exec-steps)
          exec-step-rets
          (map (fn [s]
                 (get-in s [:execution :ctx-after-exec :step-rets])) exec-steps)]
      (do
        ;; There is only 1 failure because the second is not allowed to happen.
        (t/is (= 12 success-exec-step-count))
        ;; The :step-rets key in the context of the end of each scenario should
        ;; be absent because no scenarios complete successfully.
        (t/is (= [nil nil nil]
                 [(nth exec-step-rets 4) (nth exec-step-rets 9)
                  (nth exec-step-rets 12)]))))))
