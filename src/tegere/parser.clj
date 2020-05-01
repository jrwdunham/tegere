(ns tegere.parser
  "Defines the function `parse`, which takes a Gherkin feature string and returns
  a maybe `::feature` map. Note that Gherkin `Scenario Outlines` are converted to
  seqs of `::scenario` maps; that is, the scenario outlines are expanded using
  their examples tables."
  (:require [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [instaparse.core :refer [failure?]]
            [tegere.grammar :as grammar]
            #_[tegere.utils :as u]))

(s/def ::type #{:given :when :then})
(s/def ::original-type #{:but :and})
(s/def ::text string?)
(s/def ::step
  (s/keys
   :req [::type
         ::text]
   :opt [::original-type]))
(s/def ::steps (s/coll-of ::step))
(s/def ::name string?)
(s/def ::description string?)
(s/def ::tag string?)
(s/def ::tags (s/coll-of ::tag))
(s/def ::examples-row (s/map-of string? string?))
(s/def ::scenario
  (s/keys
   :req [::description
         ::tags
         ::steps]
   :opt [::examples-row]))
(s/def ::scenarios (s/coll-of ::scenario))
(s/def ::feature
  (s/keys
   :req [::name
         ::description
         ::tags
         ::scenarios]))
(s/def ::features (s/coll-of ::feature))

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
             str/trim
             second
             (fn [n] (get-first-branch-matching-root-label
                      :FEATURE_DESCRIPTION_FRAGMENT n))))
       (str/join " ")))

(defn get-tags
  [tree]
  (->> tree
       (get-first-branch-matching-root-label :TAG_LINE)
       (get-first-branch-matching-root-label :TAG_SET)
       (get-branches-matching-root-label :TAG)
       (mapv (comp
              str/trim
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
    {::name (get-feature-name feature-block-tree)
     ::description (get-feature-description feature-block-tree)
     ::tags (get-feature-tags feature-block-tree)}))

(defn get-scen-description
  [scen-tree line-key text-key]
  (->> scen-tree
       (get-first-branch-matching-root-label line-key)
       (get-first-branch-matching-root-label text-key)
       second
       str/trim))

(defn get-scenario-description
  [scenario-tree]
  (get-scen-description scenario-tree :SCENARIO_LINE :SCENARIO_TEXT))

(defn get-scenario-outline-description
  [scenario-tree]
  (get-scen-description scenario-tree
                        :SCENARIO_OUTLINE_LINE :SCENARIO_OUTLINE_TEXT))

(defn step-str->kw
  [step-str]
  (-> step-str str/lower-case keyword))

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
       str/trim))

(defn process-step-tree
  [step-tree]
  {::type (get-step-type step-tree)
   ::text (get-step-text step-tree)})

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
       str/trim))

(defn row-trees->matrix
  [row-trees]
  (map (fn [tr-tree]
         (->> tr-tree
              (get-branches-matching-root-label :CELL)
              (map (comp str/trim second))))
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
    {::name (get-examples-name examples-tree)
     ::table (get-examples-table examples-tree)}))

(defmulti process-so-step-parts (fn [x] (first x)))

(defmethod process-so-step-parts :STEP_TEXT
  [[_ step-text]]
  [::text step-text])

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
  (mapv
   (fn [step]
     {::type (:so-step-type step)
      ::text (->> step
                      :parts
                      (map (fn [[k x]]
                             (if (= :variable-name k) (get table-row x) x)))
                      (str/join "")
                      (str/trim))})
   so-steps))

(defn scen-outln->seq-step-maps
  "Given a scenario outline parse tree and an examples table, return a sequence
  of step maps. Each step map will have a `::steps` key and an
  `::examples-row` key."
  [scenario-outline-tree examples-table]
  (let [so-steps
        (->> scenario-outline-tree
             (get-first-branch-matching-root-label :SO_STEP_BLOCK)
             (get-branches-matching-root-label :SO_STEP)
             (map pre-process-so-step))]
    (map
     (fn [table-row]
       {::steps (interpolate so-steps table-row)
        ::examples-row table-row})
     examples-table)))

(defn repair-conj-steps
  "Repair any defective (:and and :but) ::type values in steps by replacing the
  defective value with the last non-defective one. Recursive because
  (map repair (cons nil steps) steps) fails when two or more 'conj' steps are
  adjacent."
  ([steps] (repair-conj-steps steps nil))
  ([[first-step & rest-steps] prev-step-type]
   (let [first-step-type (::type first-step)
         first-step
         (if (some #{first-step-type} [:and :but])
           (-> first-step
               (assoc ::type prev-step-type)
               (assoc ::original-type first-step-type))
           first-step)]
     (if rest-steps
       (vec (cons first-step (repair-conj-steps rest-steps (::type first-step))))
       [first-step]))))

(defmulti process-scenario (fn [st] (first st)))

; Process a scenario outline tree (vector), returning a seq of maps,
; each representing a scenario.
(defmethod process-scenario :SCENARIO_OUTLINE
  [scenario-outline-tree]
  (let [tags (get-scenario-tags scenario-outline-tree)
        description (get-scenario-outline-description scenario-outline-tree)
        examples (get-examples scenario-outline-tree)
        steps-sets
        (scen-outln->seq-step-maps scenario-outline-tree (::table examples))]
    (map
     (fn [{:keys [::steps ::examples-row]}]
       {::description description
        ::examples-row examples-row
        ::tags tags
        ::steps (repair-conj-steps steps)})
     steps-sets)))

(defmethod process-scenario :SCENARIO
  [scenario-tree]
  [{::description (get-scenario-description scenario-tree)
    ::tags (get-scenario-tags scenario-tree)
    ::steps (-> scenario-tree
               get-scenario-steps
               repair-conj-steps)}])

(defn extract-scenarios
  [feature-tree]
  (->> feature-tree
       (get-branches-matching-root-labels [:SCENARIO :SCENARIO_OUTLINE])
       (map process-scenario)
       flatten
       vec))

(defn parse
  "Convert a string of Gherkin into a maybe `::feature` map. First parses the
  string into an Instaparse tree (a vector), then processes that tree to produce
  the `::feature` map. All scenario outlines are converted to `::scenario`
  maps and placed, in order,in the `::scenarios` collection."
  [feature-string]
  (let [feature-tree (grammar/feature-prsr feature-string)
        feature-block-map (process-feature-block feature-tree)
        scenarios (extract-scenarios feature-tree)
        feature (assoc feature-block-map ::scenarios scenarios)]
    feature
    #_(if (s/valid? ::feature feature)
      (u/just feature)
      (u/nothing
       {:error :invalid-feature
        :data (s/explain-data ::feature feature)}))))

(defn post-process-te
  [parsed-te]
  (if (string? parsed-te)
    parsed-te
    (let [root (first parsed-te)]
      (if (= root :NEG)
        (list 'not (post-process-te (second parsed-te)))
        (conj (map post-process-te (rest parsed-te))
              (get {:CONJ 'and :DISJ 'or} root))))))

(defn parse-tag-expression-with
  [parser te]
  (let [parse (parser te)]
    (when-not (failure? parse)
      (-> parse first post-process-te))))

(def parse-old-style-tag-expression
  "Given an old-style tag expression like 'cat,~@dog,cow,~bunny', parse it into a
  disjunction like ``(or cat (not dog) cow (not bunny))``."
  (partial parse-tag-expression-with grammar/old-style-tag-expr-prsr))

(def parse-tag-expression
  "Given a tag expression like '@wip and not @slow' parse it into an unambiguous
  ``:tegere.query/query-tree`` list like ``(and wip (not slow))``."
  (partial parse-tag-expression-with grammar/tag-expression-cli-prsr))

(defn parse-tag-expression-with-fallback
  [te]
  (or (parse-tag-expression te)
      (parse-old-style-tag-expression te)))
