(ns tegere.step-grammar
  "Experimental step grammar. This is intended to be an optional method for
  connecting step text to functions so that less boilerplate code needs to be
  written."
  (:require [clojure.string :as string]
            [instaparse.core :as insta]))

(defn construct-terminal-grammar
  [non-terminal csvs]
  (let [terminals (string/split csvs #",")
        terminals (map string/trim terminals)]
    (format "%s = '%s'\n"
            non-terminal
            (string/join "' | '" terminals))))

(def whitespace-grmr "<WS> = <#'\\s+'>\n")
(def indef-d-grmr "INDEF_D = 'a'\n")
(def def-d-grmr "DEF_D = 'the'\n")
(def d-grmr
  (str
   "D = INDEF_D | DEF_D\n"
   indef-d-grmr
   def-d-grmr))

;; Sample lexical grammars
(def sample-nouns "bid, space, inquiry")
(def sample-adjectives "Delphi-integrated, available, turned down, canceled")
(def sample-adverbs "synchronously, asynchronously")
(def sample-verbs "marked as")

;; Sample lexical grammars
(def noun-grmr "N = 'bid' | 'space' | 'inquiry'\n")
(def adj-grmr "A = 'Delphi-integrated' | 'available' | 'turned down' | 'canceled'\n")
(def adv-grmr "ADV = 'synchronously' | 'asynchronously'\n")
(def v-grmr "V = 'mark as' | 'cancel'\n")
(def v-pp-grmr "V_PP = 'marked as' | 'canceled'\n")

;; Sample functional grammars
(def p-grmr "P = 'on'\n")
(def c-grmr "C = 'that'\n")
(def copula-grmr "COPULA = 'is'\n")
(def t-grmr "T = 'is' | 'was'\n")

(def relative-clause-grmr "REL_CLAUSE = CP\n")

(def step-text-grmr
  (str
   "FRAGMENT = S | DP\n"
   "S = DP WS TP\n"
   "DP = D WS NP\n"
   "NP = N | AP WS NP | NP WS PP | NP WS REL_CLAUSE\n"
   "PP = P WS DP\n"
   "VP = V | VP WS AP | ADVP WS VP\n"
   "TP = T WS VP | T WS AP\n"
   "CP = C WS TP\n"
   "AP = A | ADVP WS AP\n"
   "ADVP = ADV\n"
   "REL_CLAUSE = CP\n"
   whitespace-grmr
   p-grmr
   adj-grmr
   adv-grmr
   noun-grmr
   v-grmr
   t-grmr
   c-grmr
   d-grmr))

(def step-text-prsr (insta/parser step-text-grmr))

(def skeleton-grammar
  (str
   "FRAGMENT = S | DP\n"
   "S = DP WS TP\n"
   "DP = D WS NP\n"
   "NP = N | AP WS NP | NP WS PP | NP WS REL_CLAUSE\n"
   "PP = P WS DP\n"
   "VP = V | VP WS AP | ADVP WS VP\n"
   "VP_PASSIVE = V_PP\n"
   "TP = T WS VP | T WS AP | T WS VP_PASSIVE\n"
   "CP = C WS TP\n"
   "AP = A | ADVP WS AP\n"
   "ADVP = ADV\n"
   "REL_CLAUSE = CP\n"
   whitespace-grmr
   p-grmr
   t-grmr
   c-grmr
   d-grmr))

(defn get-step-text-parser
  "Return a step text parser that will parse toy English. Keyword arguments can
  supply comma-separated lists of lexical items to override the defaults. Example
  usage:

    user=> ((get-step-text-parser :nouns 'dog, cat'
                                  :verbs 'running, barking, meowing'
                                  :adverbs 'quietly, loudly'
                                  :adjectives 'fat, sleepy, black')
            'the sleepy cat is quietly meowing')
    [:FRAGMENT
      [:S
        [:DP [:D [:DEF_D 'the']] [:NP [:AP [:A 'sleepy']] [:NP [:N 'cat']]]]
        [:TP [:T 'is'] [:VP [:ADVP [:ADV 'quietly']] [:VP [:V 'meowing']]]]]]"
  [& {:keys [nouns verbs adjectives adverbs]
      :or {nouns sample-nouns
           verbs sample-verbs
           adjectives sample-adjectives
           adverbs sample-adverbs}}]
  (let [step-text-grmr (str skeleton-grammar
                            (construct-terminal-grammar "N" nouns)
                            (construct-terminal-grammar "V" verbs)
                            (construct-terminal-grammar "A" adjectives)
                            (construct-terminal-grammar "ADV" adverbs))]
    (insta/parser step-text-grmr)))

(defmulti semantics (fn [parse & _] (first parse)))

(defmethod semantics :FRAGMENT [[_ root] & _] (semantics root))

(defmethod semantics :S
  [[_ DP TP]]
  (merge
   {:type :event
    :agent (semantics DP)}
   (semantics TP)))

(defmethod semantics :DP
  [[_ D NP]]
  (merge
   {:type :entity
    :quantifier (if (= (-> D second first) :DEF_D) :iota :exists)}
   (semantics NP)))

(defn phrasal-compose
  "Compute the semantics (i.e., Clojure maps) of all nodes and merge with
  concatenation of collection values, else overwrite."
  [nodes]
  (apply
   (partial
    merge-with
    (fn [old new] (if (coll? old) (concat old new) new)))
   (map semantics nodes)))

;; Phrasal Semantics simply assumes that everything but the head element is a
;; modifier that adds to the collection of :properties. The head element (e.g.,
;; V in a VP) sets a singleton attribute, e.g., :type.
(defmethod semantics :NP
  [[_ & nodes]]
  (phrasal-compose nodes))

(defmethod semantics :VP
  [[_ & nodes]]
  (phrasal-compose nodes))

(defmethod semantics :AP
  [[_ & nodes]]
  (phrasal-compose nodes))

(defmethod semantics :PP
  [[_ P DP]]
  (let [property (-> P semantics :properties first)]
    {:properties [(merge property
                         {:complement (semantics DP)})]}))

(defmethod semantics :ADVP
  [[_ & nodes]]
  (phrasal-compose nodes))

;; Lexical semantics sets type for N and V and properties collections for A and
;; ADV.
(defmethod semantics :N
  [[_ noun]]
  {:entity noun})

(defmethod semantics :V
  [[_ verb]]
  {:event verb})

(defmethod semantics :A
  [[_ adjective]]
  {:properties [{:type :predicate :predicate adjective}]})

(defmethod semantics :ADV
  [[_ adverb]]
  {:properties [{:type :predicate :predicate adverb}]})

(defmethod semantics :P
  [[_ preposition]]
  {:properties [{:type :predicate :predicate preposition}]})

(defmethod semantics :C
  [[_ complement]]
  {})

(defmethod semantics :REL_CLAUSE
  [[_ [__ ___ CP-TP]]]
  {:properties [(semantics CP-TP)]})

;; TP is vacuous for now. TODO: it should supply temporal deixis, as DP supplies
;; spatial.
(defmethod semantics :TP
  [[_ T XP]]
  (semantics XP))

(defmethod semantics :default [[_ & parts]] {})
