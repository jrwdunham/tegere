(ns tegere.grammar
  "Feature file grammar. Specifies a simple context-free grammar (CFG) for
  Gherkin feature files. This grammar (feature-grmr) is passed to
  instaparse.core/parser in order to build a parser that parses a feature
  file (a string of text) into structured data, viz. a vector-based tree
  structure."
  (:require [clojure.string :as string]
            [instaparse.core :as insta]))

(def indent-grmr "INDENT = ' '*\n")

(def new-line-grmr "NEW_LINE = '\\n'\n")

(def empty-line-grmr "EMPTY_LINE = #'\\s*\\n'\n")

(def comment-line-grmr
  (str
   "COMMENT_LINE = INDENT* COMMENT_SENTINEL+ COMMENT_TEXT* NEW_LINE\n"
   "COMMENT_SENTINEL = '#'\n"
   indent-grmr
   "COMMENT_TEXT = #'[^\\n]+'\n"
   new-line-grmr))

(def comment-line-prsr (insta/parser comment-line-grmr))

(def ignored-line-grmr
  (str
   "IGNORED_LINE = EMPTY_LINE | COMMENT_LINE\n"
   empty-line-grmr
   comment-line-grmr))

(def step-label-grmr
  (str
   "STEP_LABEL = GIVEN_LABEL | WHEN_LABEL | THEN_LABEL | CONJ_LABEL\n"
   "GIVEN_LABEL = 'Given'\n"
   "WHEN_LABEL = 'When'\n"
   "THEN_LABEL = 'Then'\n"
   "CONJ_LABEL = AND_LABEL | BUT_LABEL\n"
   "AND_LABEL = 'And'\n"
   "BUT_LABEL = 'But'\n"))

(def step-label-prsr (insta/parser step-label-grmr))

(def step-grmr
  (str
   "STEP = INDENT STEP_LABEL STEP_TEXT NEW_LINE\n"
   indent-grmr
   "STEP_TEXT = #'[^\\n]+'\n"
   step-label-grmr
   new-line-grmr))

(def step-prsr (insta/parser step-grmr))

;; "so" means "Scenario Outline". Steps in a scenario outline must be able to
;; recognize variable references between angle brackets, e.g.,
;; "Given a <modifier> monkey".
(def so-step-grmr
  (str
   "SO_STEP = INDENT STEP_LABEL (VARIABLE | STEP_TEXT)+ NEW_LINE\n"
   indent-grmr
   "VARIABLE = VLB VARIABLE_NAME VRB\n"
   "VLB = '<'\n"
   "VARIABLE_NAME = #'\\w+'\n"
   "VRB = '>'\n"
   "STEP_TEXT = #'[^\\n<>]+'\n"
   step-label-grmr
   new-line-grmr))

(def so-step-prsr (insta/parser so-step-grmr))

(def step-block-grmr
  (str
   "STEP_BLOCK = STEP (STEP | IGNORED_LINE)*\n"
   step-grmr
   ignored-line-grmr))

(def step-block-prsr (insta/parser step-block-grmr))

(def so-step-block-grmr
  (str
   "SO_STEP_BLOCK = SO_STEP (SO_STEP | IGNORED_LINE)*\n"
   so-step-grmr
   ignored-line-grmr))

(def so-step-block-prsr (insta/parser so-step-block-grmr))

(def scenario-line-grmr
  (str
   "SCENARIO_LINE = INDENT SCENARIO_LABEL SCENARIO_TEXT NEW_LINE\n"
   indent-grmr
   "SCENARIO_LABEL = 'Scenario: '\n"
   new-line-grmr
   "SCENARIO_TEXT = #'[^\\n]+'\n"))

(def scenario-line-prsr (insta/parser scenario-line-grmr))

(def scenario-outline-line-grmr
  (str
   "SCENARIO_OUTLINE_LINE = INDENT SCENARIO_OUTLINE_LABEL SCENARIO_OUTLINE_TEXT NEW_LINE\n"
   indent-grmr
   "SCENARIO_OUTLINE_LABEL = 'Scenario Outline: '"
   new-line-grmr
   "SCENARIO_OUTLINE_TEXT = #'[^\\n]+'\n"))

(def scenario-outline-line-prsr (insta/parser scenario-outline-line-grmr))

(def tag-grmr
  (str
   "TAG = TAG_SYMBOL TAG_NAME\n"
   "TAG_SYMBOL = '@'\n"
   "TAG_NAME = #'[^\\s]+'"))

(def tag-prsr (insta/parser tag-grmr))

(def tag-set-grmr
  (str
   "TAG_SET = TAG (TAG_SEP TAG)*\n"
   tag-grmr
   "TAG_SEP = ' '*\n"))

(def tag-set-prsr (insta/parser tag-set-grmr))

(def tag-line-grmr
  (str
   "TAG_LINE = INDENT TAG_SET NEW_LINE\n"
   new-line-grmr
   indent-grmr
   tag-set-grmr))

(def tag-line-prsr (insta/parser tag-line-grmr))

(def examples-line-grmr
  (str
   "EXAMPLES_LINE = INDENT EXAMPLES_LABEL EXAMPLES_TEXT NEW_LINE\n"
   indent-grmr
   "EXAMPLES_LABEL = 'Examples: '\n"
   "EXAMPLES_TEXT = #'[^\\n]+'\n"
   new-line-grmr))

(def examples-line-prsr (insta/parser examples-line-grmr))

(def table-row-grmr
  (str
   "TABLE_ROW = INDENT BORDER CELL (BORDER CELL)* BORDER INDENT NEW_LINE\n"
   indent-grmr
   "BORDER = '|'\n"
   "CELL = #'[^\\n\\|]+'\n"
   new-line-grmr))

(def table-row-prsr (insta/parser table-row-grmr))

(def table-grmr
  (str
   "TABLE = TABLE_ROW TABLE_ROW TABLE_ROW*\n"
   table-row-grmr))

(def table-prsr (insta/parser table-grmr))

(def examples-grmr
  (str
   "EXAMPLES = EXAMPLES_LINE TABLE\n"
   examples-line-grmr
   table-grmr))

(def examples-prsr (insta/parser examples-grmr))

(def scenario-grmr
  (str
   "SCENARIO = TAG_LINE? SCENARIO_LINE STEP_BLOCK\n"
   tag-line-grmr
   scenario-line-grmr
   step-block-grmr))

(def scenario-prsr (insta/parser scenario-grmr))

(def scenario-outline-grmr
  (str
   "SCENARIO_OUTLINE = TAG_LINE? SCENARIO_OUTLINE_LINE SO_STEP_BLOCK EMPTY_LINE* EXAMPLES\n"
   empty-line-grmr
   tag-line-grmr
   scenario-outline-line-grmr
   so-step-block-grmr
   examples-grmr))

(def scenario-outline-prsr (insta/parser scenario-outline-grmr))

(def feature-line-grmr
  (str
   "FEATURE_LINE = INDENT FEATURE_LABEL FEATURE_TEXT NEW_LINE\n"
   indent-grmr
   "FEATURE_LABEL = 'Feature: '\n"
   new-line-grmr
   "FEATURE_TEXT = #'[^\\n]+'\n"))

(def feature-line-prsr (insta/parser feature-line-grmr))

(def feature-description-block-grmr
  (str
   "FEATURE_DESCRIPTION_BLOCK = FEATURE_DESCRIPTION_LINE+\n"
   "FEATURE_DESCRIPTION_LINE = INDENT FEATURE_DESCRIPTION_FRAGMENT NEW_LINE\n"
   "FEATURE_DESCRIPTION_FRAGMENT = #'[^\\n]+'\n"
   indent-grmr
   new-line-grmr))

(def feature-description-block-prsr (insta/parser feature-description-block-grmr))

(def feature-block-grmr
  (str
   "FEATURE_BLOCK = TAG_LINE? FEATURE_LINE FEATURE_DESCRIPTION_BLOCK?\n"
   tag-line-grmr
   feature-line-grmr
   feature-description-block-grmr))

(def feature-block-prsr (insta/parser feature-block-grmr))

(def feature-grmr
  (str
   "FEATURE = IGNORED_LINE* FEATURE_BLOCK"
   " IGNORED_LINE* (SCENARIO | SCENARIO_OUTLINE)"
   " (IGNORED_LINE* (SCENARIO | SCENARIO_OUTLINE))*"
   " IGNORED_LINE*\n"
   ignored-line-grmr
   empty-line-grmr
   feature-block-grmr
   scenario-grmr
   scenario-outline-grmr))

(def -feature-prsr (insta/parser feature-grmr))

(defn feature-prsr
  "Parses a Gherkin feature file string into a Hiccup data format. Cheats a
  little by suffixing a newline if one is not present."
  [input]
  (let [input (if (string/ends-with? input "\n") input (str input "\n"))]
    (-feature-prsr input)))
