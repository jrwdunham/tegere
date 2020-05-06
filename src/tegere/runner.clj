(ns tegere.runner
  "Defines run, which runs a seq of features that match a supplied tags map,
  using the step functions defined in a supplied step-registry"
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [tegere.utils :as u]
            [tegere.parser :as p]
            [tegere.print :as tegprn]
            [tegere.query :as q]))

(s/def ::stop boolean?)
(s/def ::verbose boolean?)
(s/def ::data (s/map-of keyword? string?))
(s/def ::features-path string?)
(s/def ::config
  (s/keys :req [::stop
                ::verbose
                ::data
                ::q/query-tree
                ::features-path]))
(s/def ::fn fn?)
(s/def ::prev-feature ::p/feature)
(s/def ::prev-scenario ::p/scenario)

(s/def ::feature
  (s/keys
   :req [::p/name
         ::p/description
         ::p/tags]))
(s/def ::scenario
  (s/keys
   :req [::p/description
         ::p/tags]))
(s/def ::executable
  (s/keys
   :req [::p/steps
         ::feature
         ::scenario]))
(s/def ::executables (s/coll-of ::executable))

(s/def ::steps-passed integer?)
(s/def ::steps-untested integer?)
(s/def ::steps-failed integer?)
(s/def ::executions-passed integer?)
(s/def ::executions-failed integer?)
(s/def ::scenarios-passed integer?)
(s/def ::scenarios-failed integer?)
(s/def ::features-passed integer?)
(s/def ::features-failed integer?)
(s/def ::outcome #{:pass :fail :error})
(s/def ::analysis
  (s/keys
   :req [::steps-passed
         ::steps-untested
         ::steps-failed
         ::executions-passed
         ::executions-failed
         #_::outcome]))
(s/def ::feature-outcome
  (s/map-of ::scenario ::analysis))
(s/def ::run-outcome
  (s/map-of ::feature ::feature-outcome))
(s/def ::outcome-summary
  (s/keys
   :req [::features-passed
         ::features-failed
         ::scenarios-passed
         ::scenarios-failed
         ::steps-passed
         ::steps-failed
         ::steps-untested]))
(s/def ::outcome-summary-report string?)
(s/def ::start-time inst?)
(s/def ::end-time inst?)
(s/def ::ctx-after-exec (s/nilable map?))
(s/def ::type ::outcome)
(s/def ::message string?)
(s/def ::stack-trace (s/coll-of string?))
(s/def ::err
  (s/nilable
   (s/keys
    :req [::type
          ::message]
    :opt [::stack-trace])))
(s/def ::execution
  (s/nilable
   (s/keys
    :req [::start-time
          ::end-time
          ::ctx-after-exec
          ::err])))

(s/def ::run
  (s/keys
   :req [::outcome-summary
         ::outcome-summary-report
         ::executables]))

(defn features-are-empty?
  "Return true if there are no scenarios in features"
  [features]
  (->> features (map ::p/scenarios) flatten seq nil?))

(defn ensure-some-features [features]
  (if (features-are-empty? features)
    (u/nothing "No features match the supplied tags")
    (u/just features)))

(defn get-features-to-run
  "Return the substructure of the ``features`` coll such that all
  feature/scenario pairs within it match the supplied ``::tegere.query/query``
  ``query``."
  [query features]
  (-> features
      (q/query query)
      ensure-some-features))

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
        (-> step-fn-text (str/replace var-name-regex "(.+)") re-pattern)
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
  "Assign a step function, from step-registry, to each step map (under its ::fn
  key) of scenario."
  [step-registry scenario]
  (assoc
   scenario
   ::p/steps
   (map (fn [step]
          (assoc step ::fn (get-step-fn step-registry step)))
        (::p/steps scenario))))

(defn add-step-fns-to-feature
  "Assign a step function, from step-registry, to each step map (under its ::fn
  key) of each scenario of feature."
  [step-registry feature]
  (assoc feature
         ::p/scenarios
         (map (partial add-step-fns-to-scenario step-registry)
              (::p/scenarios feature))))

(defn get-missing-step-fns
  "Return the set of step maps in the features coll such that each step is
  missing a step function under ::fn."
  [features]
  (->> features
       (map (fn [feature]
              (->> feature
                   ::p/scenarios
                   (map (fn [scenario]
                          (->> scenario
                               ::p/steps
                               (filter (fn [step] (nil? (::fn step))))))))))
       flatten
       set))

(defn format-missing-step-fns
  "Return a string representation of the Clojure code that should be written in
  order to define the missing step functions."
  [missing-step-fns]
  (format "Please write step functions with the following signatures:\n%s"
          (str/join "\n\n"
                  (sort
                   (for [m missing-step-fns]
                     (format "(%s \"%s\" (fn [context] ...))"
                             (-> m ::p/type name str/capitalize)
                             (::p/text m)))))))

(defn is-executable?
  "Return an error either if features are not executable, where the second item
  in the 2-vector is a string indicating how to write the missing step
  functions."
  [features]
  (let [missing-step-fns (get-missing-step-fns features)]
    (if (seq missing-step-fns)
      (u/nothing (format-missing-step-fns missing-step-fns))
      (u/just features))))

(defn add-step-fns
  "Assign a step function, from step-registry, to each step map (under its ::fn
  key) of each scenario of each feature in features."
  [step-registry features]
  (->> features
       (map (partial add-step-fns-to-feature step-registry))
       is-executable?))

(defn handle-step-fail
  [_ e]
  (let [exc (.getMessage e)]
    (u/nothing {::type :fail ::message exc})))

(defn handle-step-error
  [_ e]
  (let [exc (.getMessage e)
        stack-trace (map str (.getStackTrace e))]
    (u/nothing {::type :error ::message exc ::stack-trace stack-trace})))

(defn call-step-fn
  "Call the step function in step, passing in context ctx. Returns 2-vec where
  the second element is an error data structure that indicates whether the step
  triggered an error or whether an assertion failed."
  [step ctx]
  (try
    (u/just ((::fn step) ctx))
    (catch AssertionError e
      (handle-step-fail step e))
    (catch Exception e
      (handle-step-error step e))))

(defn execute-step
  "Execute step by calling its step function on ctx; return a map documenting
  the execution of the step. Documents start and end times immediately before
  and after the step is executed. Catches any exception and sets the ::err key
  to a string representation of the exception. The updated context will be set
  to ::ctx-after-exec. If the return value is not a map, we embed it in a map
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
    {::start-time start-time
     ::end-time end-time
     ::ctx-after-exec ctx-after-exec
     ::err err}))

(defn execute-steps
  "Execute all steps in order by calling step-0 with ctx, then step-1 with the
  output of step-0, etc. Recursive. Each executed step will have an
  ``::execution`` key."
  [ctx [first-step & rest-steps]]
  (let [execution (if (nil? ctx) nil (execute-step first-step ctx))
        executed-step (assoc first-step ::execution execution)]
    (if rest-steps
      (cons executed-step (execute-steps (::ctx-after-exec execution) rest-steps))
      (list executed-step))))

(defn execute-executable
  [ctx {:keys [::p/steps] :as executable}]
  (println "")
  (assoc executable
         ::p/steps
         (execute-steps ctx steps)))

(defn get-executables
  "Return an ``::executables`` collection, i.e., a lazy sequence of
  ``::executable`` maps. There is one ``::executable`` for each scenario in each
  feature in the supplied coll of features."
  ([[first-feature & rest-features]]
    (get-executables first-feature rest-features (::p/scenarios first-feature)))
  ([first-feature features [first-scenario & rest-scenarios]]
   (lazy-seq
    (cons
     {::p/steps (::p/steps first-scenario)
      ::feature (select-keys first-feature [::p/name ::p/description ::p/tags])
      ::scenario (select-keys first-scenario [::p/description ::p/tags])}
     (cond rest-scenarios
           (get-executables first-feature features rest-scenarios)
           features (get-executables features)
           :else nil)))))

(defn get-valid-executables
  [features]
  (let [executables (->> features
                         get-executables
                         (filter #(-> % ::p/steps some?)))]
    (if (s/valid? ::executables executables)
      (u/just executables)
      (u/nothing
       "Failed to construct a valid collection of executable scenarios."))))

(defn executed-without-error?
  "Return true if the final step of the supplied steps map executed without
  error, false otherwise."
  [executed]
  (let [final-step (-> executed ::p/steps last)]
    (and (::execution final-step) (not (::err final-step)))))

(defn exec-executables
  "Execute all of the ``::executable`` maps in the ``::executables`` collection
  passed as the third argument. Recursive so that we can break out of execution
  if an ``::executable`` fails."
  [ctx {:keys [::stop ::prev-feature ::prev-scenario] :as config}
   [executable & rest-executables]]
  (let [feature (::feature executable)
        scenario (::scenario executable)]
    (when (or (nil? prev-feature) (not= prev-feature feature))
      (tegprn/print-feature feature))
    (when (or (nil? prev-scenario) (not= prev-scenario scenario))
      (tegprn/print-scenario scenario))
    (let [executed (execute-executable ctx executable)]
      (cons
       executed
       (if rest-executables
         (if (or (executed-without-error? executed) (not stop))
           (exec-executables
            ctx
            (-> config
                (assoc ::prev-feature feature)
                (assoc ::prev-scenario scenario))
            rest-executables)
           rest-executables)
         nil)))))

(defn execute-executables
  [ctx config executables]
  (u/just (exec-executables ctx config executables)))

(defn execute
  "Execute each scenario within each feature of features by running all of the
  step functions of each scenario in order. Return a seq of 'steps maps', maps
  that describe a single step set of a single scenario. Each step of each steps
  map each will contain an ::execution key whose value is a map representing the
  execution of the step. The stop param is a boolean; if true, then we will skip
  execution of subsequent steps after the first failure; if false, we run all
  step sets, ignoring previous failures."
  [initial-ctx stop features]
  (let [executor (partial execute-executables initial-ctx {::stop stop})]
    (u/err->> features
              get-valid-executables
              executor)))

(defn analyze-executed-executable
  "Given an executed ``::executable``, return a reduced map containing the
  original ``::feature`` and ::scenario keys as well as a new ::outcome key, whose value is a
  keyword---:pass, :fail, or :error---indicating its outcome, and count keys
  detailing how many steps passed, failed and were untested."
  [executable]
  (let [steps (::p/steps executable)
        executions (->> steps (map ::execution) (filter some?))
        last-err (->> executions last ::err)
        steps-passed (count (filter #(nil? (::err %)) executions))
        steps-untested (- (count steps) (count executions))
        steps-failed (- (count steps) steps-passed steps-untested)
        [executions-passed executions-failed]
        (if (nil? last-err) [1 0] [0 1])]
    (merge (select-keys executable [::feature ::scenario])
           {::steps-passed steps-passed
            ::steps-untested steps-untested
            ::steps-failed steps-failed
            ::executions-passed executions-passed
            ::executions-failed executions-failed
            ::outcome (get last-err ::type :pass)})))

(defn get-run-outcome
  "Convert an ``::executables`` collection to a ``::run-outcome`` map. A
  ``::run-outcome`` maps ``::feature`` maps to ``::feature-outcome`` maps, which
  in turn map ``::scenario`` maps to ``::analysis`` maps."
  [executables]
  (->> executables
       (map analyze-executed-executable)
       (reduce (fn [agg new]
                 (update-in
                  agg
                  ((juxt ::feature ::scenario) new)
                  (fn [existing-execution-analysis]
                    (merge-with
                     +
                     existing-execution-analysis
                     (select-keys new [::steps-passed
                                       ::steps-untested
                                       ::steps-failed
                                       ::executions-passed
                                       ::executions-failed])))))
               {})))

(def default-outcome-summary
  {::steps-passed 0
   ::steps-untested 0
   ::steps-failed 0
   ::scenarios-passed 0
   ::scenarios-failed 0
   ::features-passed 0
   ::features-failed 0})

(defn get-outcome-summary
  "Given a ``::run-outcome``, return an ``::outcome-summary``."
  [outcome-map]
  (reduce
   (fn [agg [_ scenarios]]
     (let [steps-stats
           (->> scenarios vals (apply merge-with +))
           scenarios-stats
           (->> scenarios
                vals
                (map (fn [step-stats-map]
                       (if (= 0 (::executions-failed step-stats-map))
                         {::scenarios-passed 1 ::scenarios-failed 0}
                         {::scenarios-passed 0 ::scenarios-failed 1})))
                (apply merge-with +))
           feature-stats
           (if (= 0 (::scenarios-failed scenarios-stats))
                  {::features-passed 1 ::features-failed 0}
                  {::features-passed 0 ::features-failed 1})]
       (merge-with +
                   agg
                   (select-keys steps-stats
                                [::steps-passed ::steps-failed ::steps-untested])
                   scenarios-stats
                   feature-stats)))
   default-outcome-summary
   outcome-map))

(defn get-outcome-summary-report
  "Convert a map summarizing the output of running the features to a
  human-readable string summarizing the same information. Input is something
  like::

      {::features {:passed 0 :failed 1}
       ::scenarios {:passed 0 :failed 1}
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
  [{:keys [::features-passed ::features-failed
           ::scenarios-passed ::scenarios-failed
           ::steps-passed ::steps-failed ::steps-untested]}]
  (format
   (str "\n%s features passed, %s failed"
        "\n%s scenarios passed, %s failed"
        "\n%s steps passed, %s failed, %s untested\n")
   features-passed features-failed
   scenarios-passed scenarios-failed
   steps-passed steps-failed steps-untested))

(defn validate-run-outcome
  [run-outcome]
  (if (s/valid? ::run-outcome run-outcome)
    run-outcome
    (throw (AssertionError. (u/pp-str run-outcome)))))

(defn summarize-run
  "Given an ``::executables`` collection, return a map with spec-conformant keys
  ``::outcome-summary`` and ``::outcome-summary-report``, the latter being a
  string like:

      0 features passed, 1 failed
      0 scenarios passed, 1 failed
      0 steps passed, 4 failed, 1 untested"
  [executables]
  (let [outcome-summary (->> executables
                             get-run-outcome
                             validate-run-outcome
                             get-outcome-summary)]
    {::outcome-summary outcome-summary
     ::outcome-summary-report (get-outcome-summary-report outcome-summary)}))

(defn validate-executables
  [executables]
  (if (s/valid? ::executables executables)
    (u/just executables)
    (u/nothing
     (format
      "Failed to construct a valid collection of executables. Error:\n%s"
      (s/explain-str ::executables executables)))))

(defn construct-run [executables]
  (u/just (assoc (summarize-run executables) ::executables executables)))

(defn run
  "Run seq of features matching tags using the step functions defined in
  step-registry. Return a ``::run`` map representing the result of running the
  features."
  ([features step-registry] (run features step-registry {}))
  ([features step-registry {query ::q/query-tree stop ::stop}
    & {:keys [initial-ctx] :or {initial-ctx {}}}]
   (u/just-then-esc
    (u/err->> features
              (partial get-features-to-run query)
              (partial add-step-fns step-registry)
              (partial execute initial-ctx stop)
              validate-executables
              construct-run)
    (fn [{:keys [::outcome-summary-report] :as run-ret}]
      (println outcome-summary-report)
      run-ret)
    (fn [error]
      (println error)
      error))))
