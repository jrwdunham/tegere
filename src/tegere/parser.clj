(ns tegere.parser
  "Defines the function `parse`, which takes a Gherkin feature string and returns
  a maybe `::feature` map. Note that Gherkin `Scenario Outlines` are converted to
  seqs of `::scenario` maps; that is, the scenario outlines are expanded using
  their examples tables."
  (:require [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [instaparse.core :refer [failure?]]
            [tegere.grammar :as grammar]))

(s/def ::type #{:given :when :then})
(s/def ::original-type #{:but :and})
(s/def ::text string?)
(s/def ::step-data-map (s/map-of keyword? string?))
(s/def ::step-data (s/coll-of ::step-data-map))
(s/def ::step
  (s/keys
   :req [::type
         ::text]
   :opt [::original-type
         ::step-data
         :tegere.runner/fn
         :tegere.runner/execution]))
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

(defn- get-all-labeled [root-label [_ & branches]]
  (->> branches
       (filter (fn [n] (= root-label (first n))))))

(defn- get-all-labeled-either [root-labels [_ & branches]]
  (->> branches
       (filter (fn [n] (some (set [(first n)]) root-labels)))))

(defn- get-first-labeled [root-label tree]
  (->> tree
       (get-all-labeled root-label)
       first))

(defn- get-feature-block [feature-tree]
  (get-first-labeled :FEATURE_BLOCK feature-tree))

(defn- get-feature-name [feature-block-tree]
  (->> feature-block-tree
       (get-first-labeled :FEATURE_LINE)
       (get-first-labeled :FEATURE_TEXT)
       second))

(defn- get-feature-description [feature-block-tree]
  (->> feature-block-tree
       (get-first-labeled :FEATURE_DESCRIPTION_BLOCK)
       (get-all-labeled :FEATURE_DESCRIPTION_LINE)
       (map (comp
             str/trim
             second
             (fn [n] (get-first-labeled
                      :FEATURE_DESCRIPTION_FRAGMENT n))))
       (str/join " ")))

(defn- get-tags [tree]
  (->> tree
       (get-first-labeled :TAG_LINE)
       (get-first-labeled :TAG_SET)
       (get-all-labeled :TAG)
       (mapv (comp
              str/trim
              second
              (fn [n] (get-first-labeled :TAG_NAME n))))))

(defn- process-feature-block [feature-tree]
  (let [feature-block-tree (get-feature-block feature-tree)]
    {::name (get-feature-name feature-block-tree)
     ::description (get-feature-description feature-block-tree)
     ::tags (get-tags feature-block-tree)}))

(defn- get-scen-description [scen-tree line-key text-key]
  (->> scen-tree
       (get-first-labeled line-key)
       (get-first-labeled text-key)
       second
       str/trim))

(defn- get-scenario-description [scenario-tree]
  (get-scen-description scenario-tree :SCENARIO_LINE :SCENARIO_TEXT))

(defn- get-scenario-outline-description [scenario-tree]
  (get-scen-description
   scenario-tree :SCENARIO_OUTLINE_LINE :SCENARIO_OUTLINE_TEXT))

(defn- step-str->kw [step-str]
  (-> step-str str/lower-case keyword))

(defn- get-step-type [step-tree]
  (let [tmp (->> step-tree (get-first-labeled :STEP_LABEL) second second)]
    (step-str->kw (if (vector? tmp) (second tmp) tmp))))

(defn- get-step-text [step]
  (->> step
       (get-first-labeled :STEP_TEXT)
       rest
       (map str/trim)
       (str/join " ")))

(defn- get-so-step-text [step]
  (let [[_ & parts] (get-first-labeled :SO_STEP_TEXT step)]
    (map (fn [[label value]]
           [(if (= :VARIABLE label) ::variable ::text) (str/trim value)])
         parts)))

(defn- strip-row [[_ & cells]]
  (map (comp str/trim second) cells))

(defn- keywordize-label [label]
  (-> label (str/replace #"\s+" "-") keyword))

(defn- process-step-data [[_ & rows]]
  (let [[top-row & rows] (map strip-row rows)
        labels (map keywordize-label top-row)]
    (mapv (fn [l r] (apply hash-map (interleave l r)))
          (repeat labels)
          rows)))

(defn- get-step-data [step]
  (get-first-labeled :STEP_DATA step))

(defn- get-processed-step-data [step]
  (when-let [step-data (get-step-data step)]
    (process-step-data step-data)))

(defn- process-step
  ([step] (process-step :scenario step))
  ([parent-type step]
   (let [text-getter (if (= :outline parent-type) get-so-step-text get-step-text)
         ret {::type (get-step-type step) ::text (text-getter step)}]
     (if-let [step-data (get-processed-step-data step)]
       (assoc ret ::step-data step-data) ret))))

(defn- get-scenario-steps [scenario]
  (->> scenario
       (get-first-labeled :STEP_BLOCK)
       (get-all-labeled :STEP)
       (map process-step)))

(defn- get-examples-name [examples-tree]
  (->> examples-tree
       (get-first-labeled :EXAMPLES_LINE)
       (get-first-labeled :EXAMPLES_TEXT)
       second
       str/trim))

(defn- row-trees->matrix [row-trees]
  (map
   (fn [tr-tree] (->> tr-tree
                      (get-all-labeled :CELL)
                      (map (comp str/trim second))))
   row-trees))

(defn- row-trees->table
  [row-trees]
  (let [[headers & row-set] (row-trees->matrix row-trees)]
    (map (fn [row] (apply hash-map (interleave headers row))) row-set)))

(defn- get-examples-table [examples-tree]
  (->> examples-tree
       (get-first-labeled :TABLE)
       (get-all-labeled :TABLE_ROW)
       (row-trees->table)))

(defn- get-examples
  [scenario-outline-tree]
  (let [examples-tree
        (get-first-labeled :EXAMPLES scenario-outline-tree)]
    {::name (get-examples-name examples-tree)
     ::table (get-examples-table examples-tree)}))

(defn- reify-parsed-text
  "Given map ``row`` mapping variable names to values, convert vector step to a
  string. ``step`` is a sequence of 2-ary vecs, where the first element is
  ``::variable`` or ``::text`` and the second element is a string, either
  variable name or literal text."
  [parsed-text row]
  (->> parsed-text
       (map (fn [[label value]] (if (= ::variable label) (get row value) value)))
       (str/join " ")
       (str/trim)))

(defn- reify-outline-step
  "Update the ``::text`` of ``::step`` by 'reifying' it, i.e., by replacing
  variable names with their values."
  [row step]
  (update step ::text reify-parsed-text row))

(defn- reify-outline-steps
  "Reify each step in ``steps`` using Scenario Outline Examples table row
  ``row``."
  [steps row]
  (mapv (partial reify-outline-step row) steps))

(defn- get-pre-processed-so-steps [scenario-outline]
  (->> scenario-outline
       (get-first-labeled :SO_STEP_BLOCK)
       (get-all-labeled :SO_STEP)
       (map (partial process-step :outline))))

(defn- get-so-rows [scenario-outline]
  (-> scenario-outline get-examples ::table))

(defn- build-proto-scenarios
  "Given an instaparsed scenario outline, return a prototype ``scenarios``
  collection, wherein each scenario has only ``::steps`` and ``::examples-row``
  keys."
  [scenario-outline]
  (let [outline-steps (get-pre-processed-so-steps scenario-outline)]
    (map
     (fn [row]
       {::steps (reify-outline-steps outline-steps row)
        ::examples-row row})
     (get-so-rows scenario-outline))))

(defn- repair-conj-steps
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

(defn- process-scenario-outline
  "Given an instaparsed ``scenario-outline``, return a ``::scenarios``
  collection."
  [scenario-outline]
  (map
   (fn [{:keys [::steps] :as scenario}]
     (merge
      scenario
      {::description (get-scenario-outline-description scenario-outline)
       ::tags (get-tags scenario-outline)
       ::steps (repair-conj-steps steps)}))
   (build-proto-scenarios scenario-outline)))

(defn- process-scenario
  "Given an instaparsed ``scenario-tree``, return a 1-ary ``::scenarios``."
  [scenario-tree]
  [{::description (get-scenario-description scenario-tree)
    ::tags (get-tags scenario-tree)
    ::steps (-> scenario-tree get-scenario-steps repair-conj-steps)}])

(defn- process-scen [[label :as scen]]
  ((if (= :SCENARIO_OUTLINE label) process-scenario-outline process-scenario) scen))

(defn- extract-scenarios
  [feature-tree]
  (->> feature-tree
       (get-all-labeled-either [:SCENARIO :SCENARIO_OUTLINE])
       (map process-scen)
       flatten
       vec))

(defn parse
  "Convert a string of Gherkin into a maybe ``::feature`` map. First parse the
  string into an Instaparse tree (a vector), then processes that tree to produce
  the ``::feature`` map. All Scenario Outlines and Scenarios are normalized to
  ``::scenario``maps and placed, in their original order, into the
  ``::scenarios`` collection."
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

;; Tag Expression (TE) Parsing

(defn- post-process-te
  [parsed-te]
  (if (string? parsed-te)
    parsed-te
    (let [root (first parsed-te)]
      (if (= root :NEG)
        (list 'not (post-process-te (second parsed-te)))
        (conj (map post-process-te (rest parsed-te))
              (get {:CONJ 'and :DISJ 'or} root))))))

(defn- parse-tag-expression-with
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
