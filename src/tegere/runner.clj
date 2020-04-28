(ns tegere.runner
  "Defines run, which runs a seq of features that match a supplied tags map,
  using the step functions defined in a supplied step-registry"
  (:require [clojure.string :as s]
            [clojure.set :refer [intersection]]
            [tegere.utils :as u]
            [tegere.parser :as p]
            [tegere.print :as tegprn]))

(defn get-scenarios-matching-pred
  "Return subset of scenarios matching pred according to their :all-tags key"
  [scenarios pred]
  (filter
   (fn [{:keys [all-tags]}] (pred all-tags))
   scenarios))

(defn get-scenarios-matching-all
  "Return subset of scenarios that have all of the and-tags in the value of
  their :all-tags keys"
  [scenarios and-tags]
  (get-scenarios-matching-pred
   scenarios
   (fn [all-tags]
     (= and-tags (intersection all-tags and-tags)))))

(defn get-scenarios-matching-any
  "Return subset of scenarios that have any of the or-tags in the value of
  their :all-tags keys"
  [scenarios or-tags]
  (get-scenarios-matching-pred
   scenarios
   (fn [all-tags]
     (seq (intersection all-tags or-tags)))))

(defn project-tags
  "Project the tags of feature to its scenario children under an :all-tags set
  key. This makes tag-based matching easier."
  [feature]
  (let [feature-tags (get feature ::p/tags ())
        scenarios (::p/scenarios feature)]
    (assoc feature
           ::p/scenarios
           (map (fn [scen]
                  (let [all-tags-set
                        (set (concat feature-tags (::p/tags scen)))]
                    (assoc scen :all-tags all-tags-set)))
                scenarios))))

(defn remove-non-matching-scenarios
  "Return feature after removing all of its scenarios that do not match tags.
  If there are and-tags, those take precedence. If there are neither and-tags
  nor or-tags, then remove no scenarios."
  [{:keys [and-tags or-tags]} {:keys [::p/scenarios] :as feature}]
  (let [matching-scenarios
        (cond
          (seq and-tags) (get-scenarios-matching-all scenarios and-tags)
          (seq or-tags) (get-scenarios-matching-any scenarios or-tags)
          :else scenarios)]
    (assoc feature ::p/scenarios matching-scenarios)))

(defn features-are-empty
  "Return true if there are no scenarios in features"
  [features]
  (->> features (map ::p/scenarios) flatten seq nil?))

(defn get-features-to-run
  "Return the features seq where each feature has had all of its scenarios
  removed that do not match tags."
  [tags features]
  (->> features
       (map project-tags)
       (map (partial remove-non-matching-scenarios tags))
       ((fn [features]
          (if (features-are-empty features)
            [nil "No features match the supplied tags"]
            [features nil])))))

(defn get-step-fn-args
  "Return a vector of arguments (strings) from step-text that match any patterns
  present in step-fn-text. If there is no match, return nil; if there are no
  args, return []. Examples:
  'I ate {fruit-type}' 'I ate a banana' => ['a banana']
  'I ate a banana'     'I ate a banana' => []
  'I saw {fruit-type}' 'I ate a banana' => nil
  'I ate a pear'       'I ate a banana' => nil"
  [step-fn-text step-text]
  (let [var-name-regex #"\{[-\w]+\}"
        step-fn-regex
        (-> step-fn-text (s/replace var-name-regex "(.+)") re-pattern)
        matches (re-find step-fn-regex step-text)]
    (if matches
      (if (sequential? matches) (rest matches) ())
      nil)))

(defn get-step-fn
  "Get the step function in step-registry that matches step. If the function
  takes arguments from the step text, we still return a unary function over
  contexts, but it is a closure that receives the required string arguments
  from the matching step text. If there are multiple matches, return the one with
  the longest step function text---a simple heuristic for the most specific
  match."
  [step-registry {step-type ::p/type step-text ::p/text}]
  (->> step-registry
       step-type
       (map (fn [[step-fn-text step-fn]]
              (let [step-fn-args (get-step-fn-args step-fn-text step-text)]
                (when step-fn-args
                  [(count step-fn-text)
                   (fn [ctx] (apply (partial step-fn ctx) step-fn-args))]))))
       (filter some?)
       sort
       last
       last))

(defn add-step-fns-to-scenario
  "Assign a step function, from step-registry, to each step map (under its :fn
  key) of scenario."
  [step-registry scenario]
  (assoc
   scenario
   ::p/steps
   (map (fn [step]
          (assoc step :fn (get-step-fn step-registry step)))
        (::p/steps scenario))))

(defn add-step-fns-to-feature
  "Assign a step function, from step-registry, to each step map (under its :fn
  key) of each scenario of feature."
  [step-registry feature]
  (assoc feature
         ::p/scenarios
         (map (partial add-step-fns-to-scenario step-registry)
              (::p/scenarios feature))))

(defn get-missing-step-fns
  "Return the set of step maps in the features coll such that each step is
  missing a step function under :fn."
  [features]
  (->> features
       (map (fn [feature]
              (->> feature
                   ::p/scenarios
                   (map (fn [scenario]
                          (->> scenario
                               ::p/steps
                               (filter (fn [step] (nil? (:fn step))))))))))
       flatten
       set))

(defn format-missing-step-fns
  "Return a string representation of the Clojure code that should be written in
  order to define the missing step functions."
  [missing-step-fns]
  (format "Please write step functions with the following signatures:\n%s"
          (s/join "\n\n"
                  (sort
                   (for [m missing-step-fns]
                     (format "(%s \"%s\" (fn [context] ...))"
                             (-> m ::p/type name s/capitalize)
                             (::p/text m)))))))

(defn is-executable?
  "Return an error either if features are not executable, where the second item
  in the 2-vector is a string indicating how to write the missing step
  functions."
  [features]
  (let [missing-step-fns (get-missing-step-fns features)]
    (if (seq missing-step-fns)
      [nil (format-missing-step-fns missing-step-fns)]
      [features nil])))

(defn add-step-fns
  "Assign a step function, from step-registry, to each step map (under its :fn
  key) of each scenario of each feature in features."
  [step-registry features]
  (->> features
       (map (partial add-step-fns-to-feature step-registry))
       is-executable?))

(defn handle-step-fail
  [_ e]
  (let [exc (.getMessage e)]
    [nil {:type :fail :message exc}]))

(defn handle-step-error
  [_ e]
  (let [exc (.getMessage e)
        stack-trace (map str (.getStackTrace e))]
    [nil {:type :error :message exc :stack-trace stack-trace}]))

(defn call-step-fn
  "Call the step function in step, passing in context ctx. Returns 2-vec where
  the second element is an error data structure that indicates whether the step
  triggered an error or whether an assertion failed."
  [step ctx]
  (try
    [((:fn step) ctx) nil]
    (catch AssertionError e
      (handle-step-fail step e))
    (catch Exception e
      (handle-step-error step e))))

(defn execute-step
  "Execute step by calling its step function on ctx; return a map documenting
  the execution of the step. Documents start and end times immediately before
  and after the step is executed. Catches any exception and sets the :err key
  to a string representation of the exception. The updated context will be set
  to :ctx-after-exec. If the return value is not a map, we embed it in a map
  under :step-return-value."
  [step ctx]
  (let [start-time (java.util.Date.)
        [ctx-after-exec err] (call-step-fn step ctx)
        end-time (java.util.Date.)
        ctx-after-exec
        (if (and (nil? err) (not (map? ctx-after-exec)))
          {:step-return-value ctx-after-exec}
          ctx-after-exec)]
    (tegprn/print-execution step err start-time end-time)
    {:start-time start-time
     :end-time end-time
     :ctx-after-exec ctx-after-exec
     :err err}))

(defn execute-steps
  "Execute all steps in order by calling step-0 with ctx, then step-1 with the
  output of step-0, etc. Recursive."
  [ctx [first-step & rest-steps]]
  (let [execution (if (nil? ctx) nil (execute-step first-step ctx))
        executed-step (assoc first-step :execution execution)]
    (if rest-steps
      (cons executed-step (execute-steps (:ctx-after-exec execution) rest-steps))
      (list executed-step))))

(defn execute-steps-map
  [ctx {:keys [::p/steps] :as steps-map}]
  (println "")
  (assoc steps-map
         ::p/steps
         (execute-steps ctx steps)))

(defn get-steps-map-seq
  "Return a lazy sequence of steps maps. These are maps with ::p/steps keys.
  There is one steps map for each scenario in each feature in the supplied
  seq of features."
  ([[first-feature & rest-features]]
    (get-steps-map-seq first-feature rest-features (::p/scenarios first-feature)))
  ([first-feature features [first-scenario & rest-scenarios]]
   (lazy-seq
    (cons
     {::p/steps (::p/steps first-scenario)
      ::p/feature (select-keys first-feature [::p/name ::p/description ::p/tags])
      ::p/scenario (select-keys first-scenario [::p/description ::p/tags])}
     (cond rest-scenarios
           (get-steps-map-seq first-feature features rest-scenarios)
           features (get-steps-map-seq features)
           :else nil)))))

(defn step-executed-without-error
  "Return true if the final step of the supplied steps map executed without
  error, false otherwise."
  [executed-steps-map]
  (let [final-step (-> executed-steps-map ::p/steps last)]
    (and (:execution final-step) (not (:err final-step)))))

(defn execute-steps-map-seq
  "Execute all of the 'steps maps' in the sequence thereof passed as the third
  argument. Recursive so that we can break out of execution if a steps map fails."
  [ctx
   {:keys [stop last-feature last-scenario] :or {stop false} :as config}
   [first-steps-map & rest-steps-maps]]
  (let [feature (::p/feature first-steps-map)
        scenario (::p/scenario first-steps-map)]
    (when (or (nil? last-feature) (not= last-feature feature))
      (tegprn/print-feature feature))
    (when (or (nil? last-scenario) (not= last-scenario scenario))
      (tegprn/print-scenario scenario))
    (let [executed-steps-map (execute-steps-map ctx first-steps-map)]
      (cons
       executed-steps-map
       (if rest-steps-maps
         (if (or (step-executed-without-error executed-steps-map) (not stop))
           (execute-steps-map-seq
            ctx
            (-> config
                (assoc :last-feature feature)
                (assoc :last-scenario scenario))
            rest-steps-maps)
           rest-steps-maps)
         nil)))))

(defn execute
  "Execute each scenario within each feature of features by running all of the
  step functions of each scenario in order. Return a seq of 'steps maps', maps
  that describe a single step set of a single scenario. Each step of each steps
  map each will contain an :execution key whose value is a map representing the
  execution of the step. The stop param is a boolean; if true, then we will skip
  execution of subsequent steps after the first failure; if false, we run all
  step sets, ignoring previous failures."
  [initial-ctx stop features]
  [(->> features
        get-steps-map-seq
        (filter #(-> % ::p/steps some?))
        ((partial execute-steps-map-seq initial-ctx {:stop stop})))
   nil])

(defn analyze-step-execution
  "Given a step execution map, return a reduced map containing the original
  ::p/feature and ::p/scenario keys as well as a new :outcome key, whose value is a
  keyword---:pass, :fail, or :error---indicating its outcome, and count keys
  detailing how many steps passed, failed and were untested."
  [execution]
  (let [steps (::p/steps execution)
        executions (->> steps (map :execution) (filter some?))
        last-err (->> executions last :err)
        step-pass-count (count (filter #(nil? (:err %)) executions))
        step-untested-count (- (count steps) (count executions))
        step-fail-count (- (count steps) step-pass-count step-untested-count)
        [execution-pass-count execution-fail-count]
        (if (nil? last-err) [1 0] [0 1])]
    (merge (select-keys execution [::p/feature ::p/scenario])
           {:step-pass-count step-pass-count
            :step-untested-count step-untested-count
            :step-fail-count step-fail-count
            :execution-pass-count execution-pass-count
            :execution-fail-count execution-fail-count
            :outcome (get last-err :type :pass)})))

(defn executions->outcome-map
  "Convert a seq of execution maps (with keys ::p/steps, ::p/feature and ::p/scenario) and
  return an outcome map, which maps features (maps) to maps from scenarios (maps)
  to maps that document step and execution pass/fail/untested counts."
  [executions]
  (->> executions
       (map analyze-step-execution)
       (reduce (fn [agg new]
                 (update-in
                  agg
                  ((juxt ::p/feature ::p/scenario) new)
                  (fn [existing-execution-analysis]
                    (merge-with
                     +
                     existing-execution-analysis
                     (select-keys new [:step-pass-count
                                       :step-untested-count
                                       :step-fail-count
                                       :execution-pass-count
                                       :execution-fail-count])))))
               {})))

(defn outcome-map->outcome-summary-map
  "Convert an outcome map to an outcome summary map. An outcome map maps features
  (themselves maps) to scenario maps, where a scenario map maps scenarios
  (themselves maps) to a map from outcome keywords (:error) to integer counts::

      {{::p/name 'Chimpanzees behave as expected'
        ::p/description 'Experimenters want to ...'
        ::p/tags ['chimpanzees']}
       {
        {::p/description 'Chimpanzees behave as expected ...'
         ::p/tags ['fruit-reactions']}
        {:error 2, :fail 2}}}

  The output is a much simpler data structure that summarizes the counts of
  features, scenarios and steps that passed and failed::

       {:feature-pass-count n
        :feature-fail-count n
        :scenario-pass-count n
        :scenario-fail-count n
        :step-pass-count n
        :step-fail-count n
        :step-untested-count n}
  "
  [outcome-map]
  (reduce
   (fn [agg [_ scenarios]]
     (let [steps-stats
           (->> scenarios vals (apply merge-with +))
           scenarios-stats
           (->> scenarios
                vals
                (map (fn [step-stats-map]
                       (if (= 0 (:execution-fail-count step-stats-map))
                         {:scenario-pass-count 1
                          :scenario-fail-count 0}
                         {:scenario-pass-count 0
                          :scenario-fail-count 1})))
                (apply merge-with +))
           feature-stats
           (if (= 0 (:scenario-fail-count scenarios-stats))
                  {:feature-pass-count 1 :feature-fail-count 0}
                  {:feature-pass-count 0 :feature-fail-count 1})]
       (merge-with +
                   agg
                   (select-keys steps-stats [:step-pass-count
                                             :step-fail-count
                                             :step-untested-count])
                   scenarios-stats
                   feature-stats)))
   {:step-pass-count 0
    :step-untested-count 0
    :step-fail-count 0
    :scenario-pass-count 0
    :scenario-fail-count 0
    :feature-pass-count 0
    :feature-fail-count 0}
   outcome-map))

(defn format-outcome-summary-map
  "Convert a map summarizing the output of running the features to a
  human-readable string summarizing the same information. Input is something
  like::

      {::p/features {:passed 0 :failed 1}
       ::p/scenarios {:passed 0 :failed 1}
       ::p/steps {:passed 0 :failed 4}}

  and the corresponding output would be::

      0 features passed, 1 failed
      0 scenarios passed, 1 failed
      0 steps passed, 4 failed, 2 untested

  TODO: add the total test execution time, e.g., 'Took 0m0.080s' and fill in the
  '???' blanks in the following prescriptive template.

      0 features passed, 1 failed, ??? skipped, ??? untested
      0 scenarios passed, 1 failed, ??? skipped, ??? untested
      0 steps passed, 4 failed, ??? skipped, ??? undefined, 2 untested
  "
  [outcome-summary-map]
  (format
   (str "\n%s features passed, %s failed"
        "\n%s scenarios passed, %s failed"
        "\n%s steps passed, %s failed, %s untested\n")
   (:feature-pass-count outcome-summary-map)
   (:feature-fail-count outcome-summary-map)
   (:scenario-pass-count outcome-summary-map)
   (:scenario-fail-count outcome-summary-map)
   (:step-pass-count outcome-summary-map)
   (:step-fail-count outcome-summary-map)
   (:step-untested-count outcome-summary-map)))

(defn get-outcome-summary
  "Return a string representation of the outcome of running all of the features.
  Something like:

  0 features passed, 1 failed
  0 scenarios passed, 1 failed
  0 steps passed, 4 failed, 1 untested
  "
  [executions & {:keys [as-data?] :or {as-data? false}}]
  (->> executions
       executions->outcome-map
       outcome-map->outcome-summary-map
       ((fn [i]
         (if as-data? i (format-outcome-summary-map i))))))

(defn run
  "Run seq of features matching tags using the step functions defined in
  step-registry"
  [features step-registry {:keys [tags stop] :or {tags {} stop false}}
   & {:keys [initial-ctx] :or {initial-ctx {}}}]
  (let [[outcome err] (u/err->> features
                                (partial get-features-to-run tags)
                                (partial add-step-fns step-registry)
                                (partial execute initial-ctx stop))]
    (if err
      (do (println err)
          err)
      (do (println (get-outcome-summary outcome))
          outcome))))
