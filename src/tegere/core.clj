(ns tegere.core
  (:require [instaparse.core :as insta]))

(def as-and-bs-grammar
  (str
   "S = AB*\n"
   "AB = A B\n"
   "A = 'a'+\n"
   "B = 'b'+\n"))

(def as-and-bs
  (insta/parser as-and-bs-grammar))


;; @generate-ar-op-cons-emails @template.template_key.<template_key>

;; Scenario: Dan wants to generate the Accounts Receivable (AR) Operating Permit (OP) renewal HTML email documents using the DGS and confirm that the generated documents have the expected properties.
;; Given a DGS instance containing an up-to-date template <template_key>, including its template dependencies
;; When a document of type <output_type> is generated from template <template_key> using data context <context_path>
;; Then the generated document is stored in the MDS
;; And the generated document is rendered correctly


(def step-label-grmr
  (str
   "STEP_LABEL = GIVEN_LABEL | WHEN_LABEL | THEN_LABEL | CONJ_LABEL\n"
   "GIVEN_LABEL = 'Given'\n"
   "WHEN_LABEL = 'When'\n"
   "THEN_LABEL = 'Then'\n"
   "CONJ_LABEL = AND_LABEL | BUT_LABEL\n"
   "AND_LABEL = 'And'\n"
   "BUT_LABEL = 'But'\n"
   ))

(def step-label-prsr (insta/parser step-label-grmr))

(def indent-grmr "INDENT = ' '*\n")

(def new-line-grmr "NEW_LINE = '\\n'\n")

(def step-grmr
  (str
   "STEP = INDENT STEP_LABEL STEP_TEXT NEW_LINE\n"
   indent-grmr
   "STEP_TEXT = #'[^\\n]+'\n"
   step-label-grmr
   new-line-grmr
   )
  )

(def step-prsr (insta/parser step-grmr))

(def step-block-grmr
  (str
   "STEP_BLOCK = STEP+\n"
   step-grmr
   ))

(def step-block-prsr (insta/parser step-block-grmr))

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
   tag-set-grmr
   ))

(def tag-line-prsr (insta/parser tag-line-grmr))

(def examples-line-grmr
  (str
   "EXAMPLES_LINE = INDENT EXAMPLES_LABEL EXAMPLES_TEXT NEW_LINE\n"
   indent-grmr
   "EXAMPLES_LABEL = 'Examples: '\n"
   "EXAMPLES_TEXT = #'[^\\n]+'\n"
   new-line-grmr
   ))

(def examples-line-prsr (insta/parser examples-line-grmr))

(def table-row-grmr
  (str
   "TABLE_ROW = INDENT BORDER CELL (BORDER CELL)* BORDER NEW_LINE\n"
   indent-grmr
   "BORDER = '|'\n"
   "CELL = #'[^\\|]+'\n"
   new-line-grmr
   ))

(def table-row-prsr (insta/parser table-row-grmr))

(def table-grmr
  (str
   "TABLE = TABLE_ROW TABLE_ROW TABLE_ROW*\n"
   table-row-grmr
   ))

(def table-prsr (insta/parser table-grmr))

(def examples-grmr
  (str
   "EXAMPLES = EXAMPLES_LINE TABLE\n"
   examples-line-grmr
   table-grmr
   ))

(def examples-prsr (insta/parser examples-grmr))

(def scenario-grmr
  (str
   "SCENARIO = TAG_LINE? SCENARIO_LINE STEP_BLOCK\n"
   tag-line-grmr
   scenario-line-grmr
   step-block-grmr
   )
  )

(def scenario-prsr (insta/parser scenario-grmr))

(def empty-line-grmr "EMPTY_LINE = #'\\s*\\n'\n")

(def scenario-outline-grmr
  (str
   "SCENARIO_OUTLINE = TAG_LINE? SCENARIO_OUTLINE_LINE STEP_BLOCK EMPTY_LINE* EXAMPLES\n"
   empty-line-grmr
   tag-line-grmr
   scenario-outline-line-grmr
   step-block-grmr
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
   new-line-grmr
   ))

(def feature-description-block-prsr (insta/parser feature-description-block-grmr))

(def feature-block-grmr
  (str
   "FEATURE_BLOCK = TAG_LINE? FEATURE_LINE FEATURE_DESCRIPTION_BLOCK?\n"
   tag-line-grmr
   feature-line-grmr
   feature-description-block-grmr
   ))

(def feature-block-prsr (insta/parser feature-block-grmr))

(def feature-grmr
  (str
   "FEATURE = FEATURE_BLOCK EMPTY_LINE* (SCENARIO | SCENARIO_OUTLINE) (EMPTY_LINE* (SCENARIO | SCENARIO_OUTLINE))?\n"
   empty-line-grmr
   feature-block-grmr
   scenario-grmr
   scenario-outline-grmr
   ))

(def feature-prsr (insta/parser feature-grmr))
