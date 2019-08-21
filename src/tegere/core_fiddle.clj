(ns tegere.core-fiddle
  "Fiddle file for playing around with core.clj."
  (:require [tegere.core :refer [main]]
            [tegere.grammar :as gr]))

(comment

  (main (list "src/examples/apes" "--tags=bonobos,monkeys" "--stop"))

  (seq (.getURLs (java.lang.ClassLoader/getSystemClassLoader)))

  ((comp inc #(* % 8)) 1)

  ((comp #(* % 8) inc) 1)

  (gr/step-label-prsr "Given")

  (gr/step-label-prsr "When")

  (gr/step-label-prsr "Then")

  (gr/step-prsr " Given a monkey\n")

  (gr/step-prsr " When I give him a banana\n")

  (gr/step-prsr " Then he is happy\n")

  (gr/step-prsr " But he doesn't eat it\n")

  (gr/step-prsr " And he looks at me quizzically\n")

  (gr/step-block-prsr
   (str
    " Given a monkey\n"
    " When I give him a banana\n"
    " Then he is happy\n"
    " But he doesn't eat it\n"
    " And he looks at me quizzically\n"
    ))

  (gr/scenario-line-prsr
   " Scenario: Monkeys are cautious when offered food.\n")

  (gr/scenario-outline-line-prsr
   " Scenario Outline: Monkeys are cautious when offered food.\n")

  (gr/tag-prsr "@ab-c_d")

  (gr/tag-set-prsr "@ab-c_d")

  (gr/tag-set-prsr "@ab-c_d @dog @cat")

  (gr/tag-line-prsr "  @ab-c_d @dog @cat\n")

  (gr/scenario-prsr
   (str
    "  Scenario: Monkeys are cautious when offered food.\n"
    "    Given a monkey\n"
    "    When I give him a banana\n"
    "    Then he is happy\n"
    "    But he doesn't eat it\n"
    "    And he looks at me quizzically\n"
    ))

  (gr/scenario-prsr
   (str
    "  @monkeys @caution-tests\n"
    "  Scenario: Monkeys are cautious when offered food.\n"
    "    Given a monkey\n"
    "    When I give him a banana\n"
    "    Then he is happy\n"
    "    But he doesn't eat it\n"
    "    And he looks at me quizzically\n"
    ))

  (gr/scenario-outline-prsr
   (str
    "  @monkeys @caution-tests\n"
    "  Scenario Outline: Monkeys are cautious when offered food.\n"
    "    Given a monkey\n"
    "    When I give him a banana\n"
    "    Then he is happy\n"
    "    But he doesn't eat it\n"
    "    And he looks at me quizzically\n"
    "   \n"
    "  Examples: monkey characteristics:\n"
    "  | h1  | h2  | h3  |\n"
    "  | d10 | d20 | d30 |\n"
    "  | d11 | d21 | d31 |\n"
    ))

  (gr/examples-line-prsr " Examples: monkey characteristics:\n")

  (gr/table-row-prsr " | a | b | c |\n")

  (gr/table-prsr
   (str
    " | h1  | h2  | h3  |\n"
    " | d10 | d20 | d30 |\n"
    " | d11 | d21 | d31 |\n"
    ))

  (gr/examples-prsr
   (str
    " Examples: monkey characteristics:\n"
    " | h1  | h2  | h3  |\n"
    " | d10 | d20 | d30 |\n"
    " | d11 | d21 | d31 |\n"
    ))

  (gr/feature-line-prsr "Feature: Monkeys behave as expected\n")

  (gr/feature-description-block-prsr
   (str
    " And my feature is so cool\n"
    " because blah blah blah\n"))

  (gr/feature-block-prsr
   (str
    "@monkeys\n"
    "Feature: Monkeys behave as expected\n"
    " And my feature is so cool\n"
    " because blah blah blah\n"
    )
   )

  (gr/feature-block-prsr
   (str
    "Feature: Monkeys behave as expected\n"
    " And my feature is so cool\n"
    " because blah blah blah\n"
    )
   )

  (gr/feature-block-prsr
   (str
    "Feature: Monkeys behave as expected\n"
    " And my feature is so cool\n"
    )
   )

  (gr/feature-block-prsr
   (str
    "Feature: Monkeys behave as expected\n"
    )
   )

  (gr/feature-prsr
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
    "    And he looks at me quizzically\n"
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
    "    And he looks at me quizzically\n"
    ))

  (gr/feature-prsr
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
    "    And he looks at me quizzically\n"
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
    "    And he looks at me quizzically\n"
    "\n"
    "\n"
    ))

  (let [real-feature
        (slurp (.getPath (clojure.java.io/resource "sample.feature")))]
    (gr/feature-prsr real-feature))

  (let [real-feature
        (slurp (.getPath (clojure.java.io/resource "sample.feature")))
        parse (gr/feature-prsr real-feature)
        [_ & nodes] parse]
    (-> parse
        count)
    (map #(if (keyword? %) % (first %)) parse)

    (map type parse)

    nodes
    (->> nodes
         (map first)
         (filter (fn [x] (not= x :IGNORED_LINE)))))

  (set [1 2 1 2])

  (not= 1 2)

)
