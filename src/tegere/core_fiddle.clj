(ns tegere.core-fiddle
  "Fiddle file for playing around with core.clj."
  (:require [tegere.core :refer :all]))

(comment

  (step-label-prsr "Given")

  (step-label-prsr "When")

  (step-label-prsr "Then")

  (step-prsr " Given a monkey\n")

  (step-prsr " When I give him a banana\n")

  (step-prsr " Then he is happy\n")

  (step-prsr " But he doesn't eat it\n")

  (step-prsr " And he looks at me quizically\n")

  (step-block-prsr
   (str
    " Given a monkey\n"
    " When I give him a banana\n"
    " Then he is happy\n"
    " But he doesn't eat it\n"
    " And he looks at me quizically\n"
    ))

  (scenario-line-prsr
   " Scenario: Monkeys are cautious when offered food.\n")

  (scenario-outline-line-prsr
   " Scenario Outline: Monkeys are cautious when offered food.\n")

  (tag-prsr "@ab-c_d")

  (tag-set-prsr "@ab-c_d")

  (tag-set-prsr "@ab-c_d @dog @cat")

  (tag-line-prsr "  @ab-c_d @dog @cat\n")

  (scenario-prsr
   (str
    "  Scenario: Monkeys are cautious when offered food.\n"
    "    Given a monkey\n"
    "    When I give him a banana\n"
    "    Then he is happy\n"
    "    But he doesn't eat it\n"
    "    And he looks at me quizically\n"
    ))

  (scenario-prsr
   (str
    "  @monkeys @caution-tests\n"
    "  Scenario: Monkeys are cautious when offered food.\n"
    "    Given a monkey\n"
    "    When I give him a banana\n"
    "    Then he is happy\n"
    "    But he doesn't eat it\n"
    "    And he looks at me quizically\n"
    ))

  (scenario-outline-prsr
   (str
    "  @monkeys @caution-tests\n"
    "  Scenario Outline: Monkeys are cautious when offered food.\n"
    "    Given a monkey\n"
    "    When I give him a banana\n"
    "    Then he is happy\n"
    "    But he doesn't eat it\n"
    "    And he looks at me quizically\n"
    "   \n"
    "  Examples: monkey characteristics:\n"
    "  | h1  | h2  | h3  |\n"
    "  | d10 | d20 | d30 |\n"
    "  | d11 | d21 | d31 |\n"
    ))

  (examples-line-prsr " Examples: monkey characteristics:\n")

  (table-row-prsr " | a | b | c |\n")

  (table-prsr
   (str
    " | h1  | h2  | h3  |\n"
    " | d10 | d20 | d30 |\n"
    " | d11 | d21 | d31 |\n"
    ))

  (examples-prsr
   (str
    " Examples: monkey characteristics:\n"
    " | h1  | h2  | h3  |\n"
    " | d10 | d20 | d30 |\n"
    " | d11 | d21 | d31 |\n"
    ))

  (feature-line-prsr "Feature: Monkeys behave as expected\n")

  (feature-description-block-prsr
   (str
    " And my feature is so cool\n"
    " because blah blah blah\n"))

  (feature-block-prsr
   (str
    "@monkeys\n"
    "Feature: Monkeys behave as expected\n"
    " And my feature is so cool\n"
    " because blah blah blah\n"
    )
   )

  (feature-block-prsr
   (str
    "Feature: Monkeys behave as expected\n"
    " And my feature is so cool\n"
    " because blah blah blah\n"
    )
   )

  (feature-block-prsr
   (str
    "Feature: Monkeys behave as expected\n"
    " And my feature is so cool\n"
    )
   )

  (feature-block-prsr
   (str
    "Feature: Monkeys behave as expected\n"
    )
   )

  (feature-prsr
   (str
    "@monkeys\n"
    "Feature: Monkeys behave as expected\n"
    "  And my feature is so cool\n"
    "  because blah blah blah\n"
    "\n"
    "  @monkeys @caution-tests\n"
    "  Scenario Outline: Monkeys are cautious when offered food.\n"
    "    Given a monkey\n"
    "    When I give him a banana\n"
    "    Then he is happy\n"
    "    But he doesn't eat it\n"
    "    And he looks at me quizically\n"
    "   \n"
    "  Examples: monkey characteristics:\n"
    "  | h1  | h2  | h3  |\n"
    "  | d10 | d20 | d30 |\n"
    "  | d11 | d21 | d31 |\n"
    "   \n"
    "  @monkeys @caution-tests\n"
    "  Scenario: Monkeys are cautious when offered food.\n"
    "    Given a monkey\n"
    "    When I give him a banana\n"
    "    Then he is happy\n"
    "    But he doesn't eat it\n"
    "    And he looks at me quizically\n"
    ))

  (feature-prsr
   (str
    "# This is a comment about this feature\n"
    "\n"
    "@monkeys\n"
    "Feature: Monkeys behave as expected\n"
    "  And my feature is so cool\n"
    "  because blah blah blah\n"
    "\n"
    "\n"
    "  # This is a comment about this scenario ...\n"
    "  @monkeys @caution-tests\n"
    "  Scenario Outline: Monkeys are cautious when offered food.\n"
    "    Given a monkey\n"
    "    When I give him a banana\n"
    "    Then he is happy\n"
    "    But he doesn't eat it\n"
    "    And he looks at me quizically\n"
    "   \n"
    "   \n"
    "  Examples: monkey characteristics:\n"
    "  | h1  | h2  | h3  |\n"
    "  | d10 | d20 | d30 |\n"
    "  | d11 | d21 | d31 |\n"
    "   \n"
    "  # This is a comment about this scenario outline...\n"
    "\n"
    "  @monkeys @caution-tests\n"
    "  Scenario: Monkeys are cautious when offered food.\n"
    "    Given a monkey\n"
    "    When I give him a banana\n"
    "    Then he is happy\n"
    "    But he doesn't eat it\n"
    "    And he looks at me quizically\n"
    "\n"
    "\n"
    ))

  (let [real-feature
        (slurp (.getPath (clojure.java.io/resource "sample.feature")))]
    (feature-prsr real-feature))

  (let [real-feature
        (slurp (.getPath (clojure.java.io/resource "sample.feature")))
        parse (feature-prsr real-feature)
        [root-label & nodes] parse]
    (-> parse
        count
        )
    (map #(if (keyword? %) % (first %)) parse)

    (map type parse)

    nodes
    (->> nodes
         (map first)
         (filter (fn [x] (not= x :IGNORED_LINE)))
         )
    )

  (set [1 2 1 2])

  (not= 1 2)

)
