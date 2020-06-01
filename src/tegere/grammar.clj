(ns tegere.grammar
  "This namespace contains Instaparse CFG (or PEG) grammars (and parsers derived
  therefrom) that are used by TeGere. The primary grammar is ``feature-prsr``,
  which describes Gherkin feature files. However, PSGs are also used to parse the
  tag expressions that may be passed at the command line, hence:
  ``tag-expression-cli-prsr`` and ``old-style-tag-expr-prsr``.

  Note that the outputs of these grammars typically require post-processing in
  order to arrive at specification-compliant Clojure data structures. For that
  post-processing work, see the ``tegere.parser`` ns.

  Final note: the grammars here could all be defined in one go as single ``def``
  statements. The present convention of breaking the grammars up into smaller
  sub-grammars is in part a vestige of how they were developed, viz., iteratively
  and via the REPL."
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

(def step-data-grmr
  (str
   "STEP_DATA = STEP_DATA_ROW+\n"
   "STEP_DATA_ROW = !( INDENT STEP_LABEL ) STEP_DATA_CELL+ <CELL_DELIM> <NEW_LINE>\n"
   "STEP_DATA_CELL = <INDENT> <CELL_DELIM> #'[^\\n|]+'\n"
   "CELL_DELIM = '|'\n"))

(def variable-grmr
  (str
   "VARIABLE = <VLB> VARIABLE_NAME <VRB>\n"
   "VLB = '<'\n"
   "<VARIABLE_NAME> = #'[a-zA-Z_0-9\\?-]+'\n"
   "VRB = '>'\n"))

(def step-grmr
  (str
   "STEP = <INDENT> STEP_LABEL STEP_TEXT STEP_DATA?\n"
   "STEP_TEXT = STEP_LINE+\n"
   "<STEP_LINE> = !( INDENT ( STEP_LABEL | '|' ) ) #'[^\\n]+' <NEW_LINE>\n"
   step-data-grmr
   indent-grmr
   step-label-grmr
   new-line-grmr))

(def step-prsr (insta/parser step-grmr))

;; "so" means "Scenario Outline". Steps in a scenario outline must be able to
;; recognize variable references between angle brackets, e.g.,
;; "Given a <modifier> chimpanzee".
(def so-step-grmr
  (str
   "SO_STEP = <INDENT> STEP_LABEL SO_STEP_TEXT STEP_DATA?\n"
   "SO_STEP_TEXT = SO_STEP_LINE+\n"
   "<SO_STEP_LINE> = !( INDENT ( STEP_LABEL | '|' ) )"
   "  ( VARIABLE | SO_STEP_STR )+ <NEW_LINE>\n"
   "SO_STEP_STR = #'[^\\n<>]+'\n"
   variable-grmr
   step-data-grmr
   indent-grmr
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
   "TABLE_ROW = INDENT ( CELL_DELIM CELL )+ CELL_DELIM INDENT NEW_LINE\n"
   indent-grmr
   "CELL_DELIM = '|'\n"
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

;; ==============================================================================
;; "Old-style" tag expression grammar
;; ==============================================================================

(def oste-tag-grmr
  (str
   "<TAG> = <[TAG_SYMBOL]> TAG_NAME\n"
   "TAG_SYMBOL = '@'\n"
   "<TAG_NAME> = #'[^\\s@~][^\\s,]*'"))

(def oste-tag-prsr (insta/parser oste-tag-grmr))

(def negated-oste-tag-grmr
  (str
   "NEG = <NEGATION> TAG\n"
   "NEGATION = '~'\n"
   oste-tag-grmr))

(def negated-oste-tag-prsr (insta/parser negated-oste-tag-grmr))

(def oste-tag-phrase-cli-grmr
  (str
   "<TAG_PHRASE> = TAG | NEG\n"
   oste-tag-grmr
   negated-oste-tag-grmr))

(def oste-tag-phrase-cli-prsr (insta/parser oste-tag-phrase-cli-grmr))

(def disjunction-oste-tags-cli-grmr
  (str
   "DISJ = TAG DISJUNCT+\n"
   "<DISJUNCT> = <WS*> <OR> <WS*> TAG_PHRASE\n"
   "OR = ','\n"
   "WS = #'\\s'\n"
   oste-tag-phrase-cli-grmr))

(def disjunction-oste-tags-cli-prsr (insta/parser disjunction-oste-tags-cli-grmr))

(def old-style-tag-expr-grmr
  (str
   "<OSTE> = DISJ | TAG_PHRASE\n"
   disjunction-oste-tags-cli-grmr))

(def old-style-tag-expr-prsr
  "This should parse old-style tag expression strings, like '@dog,~@cat', into
  raw tags, negated tag vectors, or vectors of disjoined tag expressions. The @
  sign before tags is optional and whitespace between tags is removed. Examples:

      cat                      => (cat)
      @cat                     => (cat)
      ~@cat                    => ([:NEG cat])
      cat,~@dog,cow,~bunny     => ([:DISJ cat [:NEG dog] cow [:NEG bunny]])
      cat , ~@dog,cow , ~bunny => ([:DISJ cat [:NEG dog] cow [:NEG bunny]])"
  (insta/parser old-style-tag-expr-grmr))

;; ==============================================================================
;; Tag Expression grammar
;; ==============================================================================

;; See https://cucumber.io/docs/cucumber/api/#tag-expressions
;; and https://github.com/cucumber/cucumber/tree/master/tag-expressions

(def tag-expression-cli-grmr
  "This grammar uses the PEG extensions of Instaparse in order to simulate the
  Shunting-yard algorithm. It will parse ambiguous boolean grammars such that
  (minimally) the example at
  https://github.com/cucumber/cucumber/tree/master/tag-expressions is correct::

      (= (parser/parse-tag-expression-with-fallback
           not @a or @b and not @c or not @d or @e and @f)
      '(or (or (or (not a) (and b (not c))) (not d)) (and e f)))

  This is accomplished via strategic use of the 'ordered choice' operator '/'."
  (str
   "<S> = DISJ / ( TAG | NEG | CONJ )\n"
   "<X> = NEG / ( TAG | CONJ ) / DISJ\n"
   "<Y> = TAG / ( NEG | CONJ | DISJ )\n"
   "DISJ =        <WS>* S <WS>+ <OR> <WS>+ X | "
   "       <LPAR> <WS>* S <WS>+ <OR> <WS>+ X <RPAR>\n"
   "CONJ =        <WS>* X <WS>+ <AND> <WS>+ X | "
   "       <LPAR> <WS>* X <WS>+ <AND> <WS>+ X <RPAR>\n"
   "NEG =        <WS>* <NOT> <WS>+ Y | "
   "      <LPAR> <WS>* <NOT> <WS>+ Y <RPAR>\n"
   "<TAG> = <TAG_SYMBOL> TAG_NAME\n"
   "TAG_SYMBOL = '@'\n"
   "<TAG_NAME> = #'[^\\s@()][^\\s()]*'"
   "OR = 'or'\n"
   "AND = 'and'\n"
   "NOT = 'not'\n"
   "WS = #'\\s'\n"
   "LPAR = '('\n"
   "RPAR = ')'\n"))

(def tag-expression-cli-prsr (insta/parser tag-expression-cli-grmr))

(comment

  (insta/parse
   tag-expression-cli-prsr
   "@a and @b")

  (insta/parses
   tag-expression-cli-prsr
   "not @a or @b and not @c or not @d or @e and @f")

)
