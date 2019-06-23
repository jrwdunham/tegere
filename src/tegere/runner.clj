(ns tegere.runner
  "Defines run, which runs a seq of features that match a supplied tags map,
  using the step functions defined in a supplied step-registry"
  (:require [clojure.string :as s]
            [clojure.set :refer [intersection]]
            [tegere.utils :refer [bind err->>]]))

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
  (let [feature-tags (get feature :tags ())
        scenarios (:scenarios feature)]
    (assoc feature
           :scenarios
           (map (fn [scen]
                  (let [all-tags-set
                        (set (concat feature-tags (:tags scen)))]
                    (assoc scen :all-tags all-tags-set)))
                scenarios))))

(defn remove-non-matching-scenarios
  "Return feature after removing all of its scenarios that do not match tags.
  If there are and-tags, those take precedence. If there are neither and-tags
  nor or-tags, then remove no scenarios."
  [{:keys [and-tags or-tags]} {:keys [scenarios] :as feature}]
  (let [matching-scenarios
        (cond
          (seq and-tags) (get-scenarios-matching-all scenarios and-tags)
          (seq or-tags) (get-scenarios-matching-any scenarios or-tags)
          :else scenarios)]
    (assoc feature :scenarios matching-scenarios)))

(defn features-are-empty
  "Return true if there are no scenarios in features"
  [features]
  (->> features (map :scenarios) flatten seq nil?))

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
        step-fn-vars (re-seq var-name-regex step-fn-text)
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
  from the matching step text."
  [step-registry {step-type :type step-text :text}]
  (->> step-registry
       step-type
       (map (fn [[step-fn-text step-fn]]
              (let [step-fn-args (get-step-fn-args step-fn-text step-text)]
                (if (seq step-fn-args)
                  (fn [ctx] (apply (partial step-fn ctx) step-fn-args))
                  step-fn))))
       (filter some?)
       first))

(defn add-step-fns-to-scenario
  "Assign a step function, from step-registry, to each step map (under its :fn
  key) of scenario."
  [step-registry scenario]
  (assoc
   scenario
   :steps
   (map (fn [step]
          (assoc step :fn (get-step-fn step-registry step)))
        (:steps scenario))))

(defn add-step-fns-to-feature
  "Assign a step function, from step-registry, to each step map (under its :fn
  key) of each scenario of feature."
  [step-registry feature]
  (assoc feature
         :scenarios
         (map (partial add-step-fns-to-scenario step-registry)
              (:scenarios feature))))

(defn get-missing-step-fns
  "Return the set of step maps in the features coll such that each step is
  missing a step function under :fn."
  [features]
  (->> features
       (map (fn [feature]
              (->> feature
                   :scenarios
                   (map (fn [scenario]
                          (->> scenario
                               :steps
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
                             (-> m :type name s/capitalize)
                             (:text m)))))))

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

(defn execute-step
  "Execute step by calling its step function on ctx; return a map documenting
  the execution of the step. Documents start and end times immediately before
  and after the step is executed. Catches any exception and sets the :err key
  to a string representation of the exception. The updated context will be set
  to :ctx-after-exec. If the return value is not a map, we embed it in a map
  under :step-retern-value."
  [step ctx]
  (let [start-time (java.util.Date.)
        [ctx-after-exec err]
        (try
          [((:fn step) ctx) nil]
          (catch Exception e [nil (str "caught exception: " (.getMessage e))]))
        end-time (java.util.Date.)
        ctx-after-exec
        (if (and (nil? err) (not (map? ctx-after-exec)))
          {:step-return-value ctx-after-exec}
          ctx-after-exec)]
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
  [ctx {:keys [steps] :as steps-map}]
  (assoc steps-map
         :steps
         (execute-steps ctx steps)))

(defn get-steps-map-seq
  "Return a lazy sequence of steps maps. These are maps with :steps keys.
  There is one steps map for each scenario in each feature in the supplied
  seq of features."
  ([[first-feature & rest-features]]
    (get-steps-map-seq first-feature rest-features (:scenarios first-feature)))
  ([first-feature features [first-scenario & rest-scenarios]]
   (lazy-seq
    (cons
     {:steps (:steps first-scenario)
      :feature (select-keys first-feature [:name :description :tags])
      :scenario (select-keys first-scenario [:description :tags])}
     (cond rest-scenarios
           (get-steps-map-seq first-feature features rest-scenarios)
           features (get-steps-map-seq features)
           :else nil)))))

(defn step-executed-without-error
  "Return true if the final step of the supplied steps map executed without
  error, false otherwise."
  [executed-steps-map]
  (let [final-step (-> executed-steps-map :steps last)]
    (and (:execution final-step) (not (:err final-step)))))

(defn execute-steps-map-seq
  "Execute all of the 'steps maps' in the sequence thereof passed as the third
  argument. Recursive so that we can break out of execution if a steps map fails."
  [ctx stop [first-steps-map & rest-steps-maps]]
  (let [executed-steps-map (execute-steps-map ctx first-steps-map)]
    (cons
     executed-steps-map
     (if rest-steps-maps
       (if (or (step-executed-without-error executed-steps-map) (not stop))
         (execute-steps-map-seq ctx stop rest-steps-maps)
         rest-steps-maps)
       nil))))

(defn execute
  "Execute each scenario within each feature of features by running all of the
  step functions of each scenario in order. Return a seq of 'steps maps', maps
  that describe a single step set of a single scenario. Each step of each steps
  map each will contain an :execution key whose value is a map representing the
  execution of the step. The stop param is a boolean; if true, then we will skip
  execution of subsequent steps after the first failure; if false, we run all
  step sets, ignoring previous failures."
  [initial-ctx stop features]
  (->> features
       get-steps-map-seq
       ((partial execute-steps-map-seq initial-ctx stop))))

(defn run
  "Run seq of features matching tags using the step functions defined in
  step-registry"
  [features step-registry {:keys [tags stop] :or {tags {} stop false}}
   & {:keys [initial-ctx] :or {initial-ctx {}}}]
  (err->> features
          (partial get-features-to-run tags)
          (partial add-step-fns step-registry)
          (partial execute initial-ctx stop)))
