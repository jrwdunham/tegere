(ns tegere.step-grammar-fiddle
  "Fiddle file for playing around with step_grammar.clj."
  (:require [clojure.string :as string]
            [tegere.step-grammar :refer :all]
            [instaparse.core :as insta]))

(def sample-np-indef-det "a bid")
(def sample-np-def-det "the bid")
(def sample-np-indef-det-adj "a Delphi-integrated bid")
(def sample-np-indef-det-pp-mod "a bid on a space")
(def sample-np-indef-det-adj-pp-mod "a Delphi-integrated bid on a space")
(def sample-np-cp-mod "a space that is available")
(def sample-np-indef-det-adj-pp-mod-cp-mod
  "a Delphi-integrated bid on a space that is available")
(def sample-s "the inquiry is synchronously marked as turned down")
(def sample-s-2 "the bid is canceled")

(def sample-s-parse
  [:FRAGMENT
   [:S
    [:DP [:D [:DEF_D "the"]] [:NP [:AP [:A "sleepy"]] [:NP [:N "cat"]]]]
    [:TP [:T "is"] [:VP [:ADVP [:ADV "quietly"]] [:VP [:V "meowing"]]]]]])

(comment

  (string/split "dog, cat" #",")

  (string/join "' | '" ["dog" "cat"])

  (construct-terminal-grammar "N" "dog, cat, big rabbit")

  (println step-text-grmr)

  (step-text-prsr sample-np-indef-det)

  (step-text-prsr sample-np-def-det)

  (step-text-prsr sample-np-indef-det-adj)

  (step-text-prsr sample-np-indef-det-pp-mod)

  (step-text-prsr sample-np-indef-det-adj-pp-mod)

  (step-text-prsr sample-np-cp-mod)

  (step-text-prsr sample-np-indef-det-adj-pp-mod-cp-mod)

  (insta/parses step-text-prsr sample-np-indef-det-adj-pp-mod-cp-mod)

  (step-text-prsr sample-s)

  (step-text-prsr sample-s-2)

  ((get-step-text-parser) sample-s)

  ((get-step-text-parser :nouns "dog, cat"
                         :verbs "running, barking, meowing"
                         :adverbs "quietly, loudly"
                         :adjectives "fat, sleepy, black")
   "the sleepy cat is quietly meowing")

  sample-s-parse

  (semantics sample-s-parse)

  (let [parser
        (get-step-text-parser :nouns "dog, cat"
                              :verbs "running, barking, meowing"
                              :adverbs "quietly, loudly"
                              :adjectives "fat, sleepy, black")
        input "the fat sleepy cat is quietly meowing"
        parse (parser input)]
    [parse (semantics parse)])

  (let [parser (get-step-text-parser)
        input "a bid"  ;; {:type :entity, :quantifier :exists, :entity-type "bid"}
        input "the bid"  ;; {:type :entity, :quantifier :iota, :entity-type "bid"}
        input "a canceled Delphi-integrated bid on a space"
        input sample-np-indef-det-adj-pp-mod
        input sample-np-indef-det
        input sample-np-def-det
        input sample-np-indef-det-adj
        input sample-np-indef-det-pp-mod
        input sample-np-indef-det-adj-pp-mod
        input sample-np-cp-mod
        input sample-np-indef-det-adj-pp-mod-cp-mod
        input sample-s  ;; TODO: "marked as turned down" does NOT mean that there is a "marked as" event with the property "turned down"
        ;; input sample-s-2
        parse (parser input)]
    [input parse (semantics parse)])

  (update {:a (list 1)} :a conj 2)

  (merge-with (fn [old new] (if (coll? old) (concat old new) new)) {:a 2} {:a 22})

  (merge-with
   (fn [old new] (if (coll? old) (concat old new) new))
   {:a [2]} {:a [22]} {:b 3})

  (-> "the cat" (string/replace #"\s+" "-") keyword)


)
