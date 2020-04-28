(ns tegere.fiddle.grammar
  "Fiddle file for playing around with grammar.clj."
  (:require [clojure.string :as str]
            [tegere.grammar :as gr]))

(def chimpanzee-feature
  (str
   "# This is a comment about this feature\n"
   "\n"
   "@chimpanzees\n"
   "Feature: Chimpanzees behave as expected\n"
   "  Experimenters want to ensure that their chimpanzee simulations are behaving\n"
   "  correctly.\n"
   "\n"
   "\n"
   "  # This is a comment about this scenario ...\n"
   "  @fruit-reactions\n"
   "  Scenario Outline: Chimpanzees behave as expected when offered various foods.\n"
   "    Given a chimpanzee\n"
   "    When I give him a <fruit>\n"
   "    Then he is <response>\n"
   "    But he doesn't eat it\n"
   "    And he looks at me <manner_of_looking>\n"
   "   \n"
   "   \n"
   "  Examples: chimpanzee characteristics:\n"
   "  | fruit  | response  | manner_of_looking  |\n"
   "  | banana | happy     | quizzically        |\n"
   "  | pear   | sad       | loathingly         |\n"
   "   \n"
   "  # This is a comment about this scenario outline...\n"
   "\n"
   "  @orangutan\n"
   "  Scenario: Chimpanzees adore orangutans.\n"
   "    Given a chimpanzee\n"
   "    When I present him with an orangutan\n"
   "    Then he is happy\n"
   "\n"
   "\n"))

(comment

  (gr/step-label-prsr "Given")

  (gr/step-label-prsr "When")

  (gr/step-label-prsr "Then")

  (gr/step-label-prsr "But")

  (gr/step-prsr " Given a chimpanzee\n")

  (gr/step-prsr " When I give him a banana\n")

  (gr/step-prsr " Then he is happy\n")

  (gr/step-prsr " But he doesn't eat it\n")

  (gr/step-prsr " And he looks at me quizzically\n")

  (gr/step-block-prsr
   (str
    " Given a chimpanzee\n"
    " When I give him a banana\n"
    " Then he is happy\n"
    " But he doesn't eat it\n"
    " And he looks at me quizzically\n"))

  ;; comments work inside step blocks and scenario outline step blocks:
  (gr/step-block-prsr
   (str
    " Given a chimpanzee\n"
    " #When I give him a banana\n"
    " # Then he is happy\n"
    "    #But he doesn't eat it\n"
    "    #    And he looks at me quizzically\n"))

  ;; comments work inside step blocks and scenario outline step blocks:
  (gr/so-step-block-prsr
   (str
    " Given a chimpanzee\n"
    " #When I give him a banana\n"
    " Then he is happy\n"
    " But he doesn't eat it\n"
    " And he looks at me quizzically\n"))

  (gr/scenario-line-prsr
   " Scenario: Chimpanzees are cautious when offered food.\n")

  (gr/scenario-outline-line-prsr
   " Scenario Outline: Chimpanzees are cautious when offered food.\n")

  (gr/tag-prsr "@ab-c_d")

  (gr/tag-set-prsr "@ab-c_d")

  (gr/tag-set-prsr "@ab-c_d @dog @cat")

  (gr/tag-line-prsr "  @ab-c_d @dog @cat\n")

  (gr/scenario-prsr
   (str
    "  Scenario: Chimpanzees are cautious when offered food.\n"
    "    Given a chimpanzee\n"
    "    When I give him a banana\n"
    "    Then he is happy\n"
    "    But he doesn't eat it\n"
    "    And he looks at me quizzically\n"))

  (gr/scenario-prsr
   (str
    "  @chimpanzees @caution-tests\n"
    "  Scenario: Chimpanzees are cautious when offered food.\n"
    "    Given a chimpanzee\n"
    "    When I give him a banana\n"
    "    Then he is happy\n"
    "    But he doesn't eat it\n"
    "    And he looks at me quizzically\n"))

  (gr/scenario-outline-prsr
   (str
    "  @chimpanzees @caution-tests\n"
    "  Scenario Outline: Chimpanzees are cautious when offered food.\n"
    "    Given a chimpanzee\n"
    "    When I give him a banana\n"
    "    Then he is happy\n"
    "    But he doesn't eat it\n"
    "    And he looks at me quizzically\n"
    "   \n"
    "  Examples: chimpanzee characteristics:\n"
    "  | h1  | h2  | h3  |\n"
    "  | d10 | d20 | d30 |\n"
    "  | d11 | d21 | d31 |\n"))

  (gr/examples-line-prsr " Examples: chimpanzee characteristics:\n")

  (gr/table-row-prsr " | a | b | c |\n")

  (gr/table-prsr
   (str
    " | h1  | h2  | h3  |\n"
    " | d10 | d20 | d30 |\n"
    " | d11 | d21 | d31 |\n"
    ))

  (gr/examples-prsr
   (str
    " Examples: chimpanzee characteristics:\n"
    " | h1  | h2  | h3  |\n"
    " | d10 | d20 | d30 |\n"
    " | d11 | d21 | d31 |\n"
    ))

  (gr/feature-line-prsr "Feature: Chimpanzees behave as expected\n")

  (gr/feature-description-block-prsr
   (str
    " And my feature is so cool\n"
    " because blah blah blah\n"))

  (gr/feature-block-prsr
   (str
    "@chimpanzees\n"
    "Feature: Chimpanzees behave as expected\n"
    " And my feature is so cool\n"
    " because blah blah blah\n"
    )
   )

  (gr/feature-block-prsr
   (str
    "Feature: Chimpanzees behave as expected\n"
    " And my feature is so cool\n"
    " because blah blah blah\n"
    )
   )

  (gr/feature-block-prsr
   (str
    "Feature: Chimpanzees behave as expected\n"
    " And my feature is so cool\n"
    )
   )

  (gr/feature-block-prsr
   (str
    "Feature: Chimpanzees behave as expected\n"
    )
   )

  (gr/feature-prsr
   (str
    "@chimpanzees\n"
    "Feature: Chimpanzees behave as expected\n"
    "  And my feature is so cool\n"
    "  because blah blah blah\n"
    "\n"
    "  @chimpanzees @caution-tests\n"
    "  Scenario Outline: Chimpanzees are cautious when offered food.\n"
    "    Given a chimpanzee\n"
    "    When I give him a banana\n"
    "    Then he is happy\n"
    "    But he doesn't eat it\n"
    "    And he looks at me quizzically\n"
    "   \n"
    "  Examples: chimpanzee characteristics:\n"
    "  | h1  | h2  | h3  |\n"
    "  | d10 | d20 | d30 |\n"
    "  | d11 | d21 | d31 |\n"
    "   \n"
    "  @chimpanzees @caution-tests @a.b.<c>\n"
    "  Scenario: Chimpanzees are cautious when offered food.\n"
    "    Given a chimpanzee\n"
    "    When I give him a banana\n"
    "    Then he is happy\n"
    "    But he doesn't eat it\n"
    "    And he looks at me quizzically\n"))

  (gr/feature-prsr chimpanzee-feature)

  ;; Show that we can parse feature files that do not end with a newline.
  (gr/feature-prsr (str/trim chimpanzee-feature))

  (let [real-feature
        (slurp (.getPath (clojure.java.io/resource "sample.feature")))]
    (gr/feature-prsr real-feature))

  (gr/so-step-prsr "Given a <modifier> chimpanzee\n")

  (gr/so-step-prsr "Given a <modifier> chimpanzee of <qualifier> character\n")

  (gr/so-step-block-prsr
   (str
    "Given a <modifier> chimpanzee of <qualifier> character\n"
    "When experimenter approaches <adverb>\n"))

  gr/feature-grmr

)
