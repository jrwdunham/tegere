(ns tegere.runner
  "Defines run, which runs a seq of features that match a supplied tags map,
  using the step functions defined in a supplied step-registry"
  (:require [clojure.set :refer [intersection]]))

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
  "Project the tags of feature to its scenario children under a :all-tags set
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

(defn get-features-to-run
  "Return the features seq where each feature has had all of its scenarios
  removed that do not match tags."
  [features tags]
  (->> features
       (map project-tags)
       (map (partial remove-non-matching-scenarios tags))))

(defn get-step-fns
  "Get the step functions in step-registry that match step.
  TODO: :text will be a regular expression in some cases; account for this!"
  [step-registry step]
  (get-in step-registry ((juxt :type :text) step)))

(defn add-step-fns-to-scenario
  "Assign a step function, from step-registry, to each step map (under its :fn
  key) of scenario."
  [step-registry scenario]
  (assoc
   scenario
   :steps
   (map (fn [step]
          (assoc step :fn (get-step-fns step-registry step)))
        (:steps scenario))))

(defn add-step-fns-to-feature
  "Assign a step function, from step-registry, to each step map (under its :fn
  key) of each scenario of feature."
  [step-registry feature]
  (assoc feature
         :scenarios
         (map (partial add-step-fns-to-scenario step-registry)
              (:scenarios feature))))

(defn add-step-fns
  "Assign a step function, from step-registry, to each step map (under its :fn
  key) of each scenario of each feature in features."
  [features step-registry]
  (map (partial add-step-fns-to-feature step-registry) features))

(defn get-missing-step-fns
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

(defn is-executable?
  [features]
  (let [missing-step-fns (get-missing-step-fns features)]
    (if (seq missing-step-fns)
      [false missing-step-fns]
      [true nil])))

(defn bind
  "See https://adambard.com/blog/acceptable-error-handling-in-clojure/."
  [f [val err]]
  (if (nil? err)
    (f val)
    [nil err]))

(defmacro err->>
  [val & fns]
  `(->> [~val nil]
        ~@(map (fn [f]
                 `(bind ~f))
               fns)))

(defn run
  "Run seq of features matching tags using the step functions defined in
  step-registry"
  [features tags step-registry]
  (let [features-to-run (get-features-to-run features tags)
        features-with-step-fns (add-step-fns features-to-run step-registry)
        is-executable (is-executable? features-with-step-fns)]
    [is-executable features-with-step-fns]))
