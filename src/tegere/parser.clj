(ns tegere.parser
  "Parser: implementation of parse, which converts a feature file to a map
  representing the feature. That map has keys :name, :description, :tags,
  :scenarios. The scenarios value is a seq of scenario maps. Each scenario map
  has keys :description, :tags and :steps. The steps value is a seq of step maps.
  Each step map has :type and :text keys. All Scenario Outlines are converted
  to seqs of scenario maps; that is, the scenario outlines are expanded using
  their examples tables."
  (:require [clojure.string :as s]
            [tegere.grammar :refer [feature-prsr]]))

(defn get-branches-matching-root-label
  [root-label [_ & branches]]
  (->> branches
       (filter (fn [n] (= root-label (first n))))))

(defn get-branches-matching-root-labels
  [root-labels [_ & branches]]
  (->> branches
       (filter (fn [n] (some (set [(first n)]) root-labels)))))

(defn get-first-branch-matching-root-label
  [root-label tree]
  (->> tree
       (get-branches-matching-root-label root-label)
       first))

(defn get-feature-block
  [feature-tree]
  (get-first-branch-matching-root-label
   :FEATURE_BLOCK feature-tree))

(defn get-feature-name
  [feature-block-tree]
  (->> feature-block-tree
       (get-first-branch-matching-root-label :FEATURE_LINE)
       (get-first-branch-matching-root-label :FEATURE_TEXT)
       second))

(defn get-feature-description
  [feature-block-tree]
  (->> feature-block-tree
       (get-first-branch-matching-root-label :FEATURE_DESCRIPTION_BLOCK)
       (get-branches-matching-root-label :FEATURE_DESCRIPTION_LINE)
       (map (comp
             s/trim
             second
             (fn [n] (get-first-branch-matching-root-label
                      :FEATURE_DESCRIPTION_FRAGMENT n))))
       (s/join " ")))

(defn get-tags
  [tree]
  (->> tree
       (get-first-branch-matching-root-label :TAG_LINE)
       (get-first-branch-matching-root-label :TAG_SET)
       (get-branches-matching-root-label :TAG)
       (map (comp
             s/trim
             second
             (fn [n] (get-first-branch-matching-root-label :TAG_NAME n))))))

(defn get-feature-tags
  [feature-block-tree]
  (get-tags feature-block-tree))

(defn get-scenario-tags
  [scenario-tree]
  (get-tags scenario-tree))

(defn process-feature-block
  [feature-tree]
  (let [feature-block-tree (get-feature-block feature-tree)]
    {:name (get-feature-name feature-block-tree)
     :description (get-feature-description feature-block-tree)
     :tags (get-feature-tags feature-block-tree)}))

(defn get-scen-description
  [scen-tree line-key text-key]
  (->> scen-tree
       (get-first-branch-matching-root-label line-key)
       (get-first-branch-matching-root-label text-key)
       second
       s/trim))

(defn get-scenario-description
  [scenario-tree]
  (get-scen-description scenario-tree :SCENARIO_LINE :SCENARIO_TEXT))

(defn get-scenario-outline-description
  [scenario-tree]
  (get-scen-description scenario-tree
                        :SCENARIO_OUTLINE_LINE :SCENARIO_OUTLINE_TEXT))

(defn step-str->kw
  [step-str]
  (-> step-str s/lower-case keyword))

(defn get-step-type
  [step-tree]
  (let [tmp
        (->> step-tree
             (get-first-branch-matching-root-label :STEP_LABEL)
             second
             second)]
    (-> (if (vector? tmp) (second tmp) tmp)
        step-str->kw)))

(defn get-step-text
  [step-tree]
  (->> step-tree
       (get-first-branch-matching-root-label :STEP_TEXT)
       second
       s/trim))

(defn process-step-tree
  [step-tree]
  {:type (get-step-type step-tree)
   :text (get-step-text step-tree)})

(defn get-scenario-steps
  [scenario-tree]
  (->> scenario-tree
       (get-first-branch-matching-root-label :STEP_BLOCK)
       (get-branches-matching-root-label :STEP)
       (map process-step-tree)))

(defn get-examples-name
  [examples-tree]
  (->> examples-tree
       (get-first-branch-matching-root-label :EXAMPLES_LINE)
       (get-first-branch-matching-root-label :EXAMPLES_TEXT)
       second
       s/trim))

(defn row-trees->matrix
  [row-trees]
  (map (fn [tr-tree]
         (->> tr-tree
              (get-branches-matching-root-label :CELL)
              (map (comp s/trim second))))
       row-trees))

(defn row-trees->table
  [row-trees]
  (let [[headers & row-set] (row-trees->matrix row-trees)]
    (map (fn [row]
           (apply hash-map (interleave headers row)))
         row-set)))

(defn get-examples-table
  [examples-tree]
  (->> examples-tree
       (get-first-branch-matching-root-label :TABLE)
       (get-branches-matching-root-label :TABLE_ROW)
       (row-trees->table)))

(defn get-examples
  [scenario-outline-tree]
  (let [examples-tree 
        (get-first-branch-matching-root-label :EXAMPLES scenario-outline-tree)]
    {:name (get-examples-name examples-tree)
     :table (get-examples-table examples-tree)}))

(defmulti process-so-step-parts (fn [x] (first x)))

(defmethod process-so-step-parts :STEP_TEXT
  [[_ step-text]]
  [:text step-text])

(defmethod process-so-step-parts :VARIABLE
  [[_ _ variable-name _]]
  [:variable-name (second variable-name)])

(defn pre-process-so-step
  [so-step-tree]
  (let [so-step-type (get-step-type so-step-tree)
        parts
        (->> so-step-tree
             (get-branches-matching-root-labels [:STEP_TEXT :VARIABLE])
             (map process-so-step-parts))]
    {:so-step-type so-step-type
     :parts parts}))

(defn interpolate
  [so-steps table-row]
  (map
   (fn [step]
     {:type (:so-step-type step)
      :text (->> step
                      :parts
                      (map (fn [[k x]]
                             (if (= :variable-name k) (get table-row x) x)))
                      (s/join "")
                      (s/trim))})
   so-steps))

(defn scenario-outline->scenarios
  [scenario-outline-tree examples-table]
  (let [so-steps
        (->> scenario-outline-tree
             (get-first-branch-matching-root-label :SO_STEP_BLOCK)
             (get-branches-matching-root-label :SO_STEP)
             (map pre-process-so-step))]
    (map
     (fn [table-row] (interpolate so-steps table-row))
     examples-table)))

(defn repair-conj-steps
  "Repair any defective (:and and :but) :type values in steps by replacing the
  defective value with the last non-defective one. Recursive because
  (map repair (cons nil steps) steps) fail when two or more 'conj' steps are
  adjacent."
  ([steps] (repair-conj-steps steps nil))
  ([[first-step & rest-steps] prev-step-type]
   (let [first-step-type (:type first-step)
         first-step
         (if (some #{first-step-type} [:and :but])
           (assoc first-step :type prev-step-type)
           first-step)]
     (if rest-steps
       (cons first-step (repair-conj-steps rest-steps (:type first-step)))
       (list first-step)))))

(defmulti process-scenario (fn [st] (first st)))

; Process a scenario outline tree (vector), returning a seq of maps,
; each representing a scenario.
(defmethod process-scenario :SCENARIO_OUTLINE
  [scenario-outline-tree]
  (let [tags (get-scenario-tags scenario-outline-tree)
        description (get-scenario-outline-description scenario-outline-tree)
        examples (get-examples scenario-outline-tree)
        steps-sets
        (scenario-outline->scenarios scenario-outline-tree (:table examples))]
    (map
     (fn [steps]
       {:description description
        :tags tags
        :steps (repair-conj-steps steps)})
     steps-sets)))

(defmethod process-scenario :SCENARIO
  [scenario-tree]
  [{:description (get-scenario-description scenario-tree)
    :tags (get-scenario-tags scenario-tree)
    :steps (-> scenario-tree
               get-scenario-steps
               repair-conj-steps)}])

(defn extract-scenarios
  [feature-tree]
  (->> feature-tree
       (get-branches-matching-root-labels [:SCENARIO :SCENARIO_OUTLINE])
       (map process-scenario)
       flatten))

(defn parse
  "Convert an Instaparse tree (vector) to data: a Clojure map that represents
  the feature. It should have keys for name, description, tags and scenarios.
  The value of scenarios should be a vector of scenario maps.
  All scenario outlines should be evaluated to scenarios and placed, in order,
  in the scenarios vector. "
  [feature-string]
  (let [feature-tree (feature-prsr feature-string)
        feature-block-map (process-feature-block feature-tree)
        scenarios (extract-scenarios feature-tree)]
    (merge feature-block-map
           {:scenarios scenarios})))
