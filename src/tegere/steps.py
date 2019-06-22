(ns tegere.runner
  (:require [clojure.set :refer [intersection]]
            [tegere.parser :refer [parse]]))

(defn get-scenarios-matching-pred
  [scenarios pred]
  (filter
   (fn [{:keys [all-tags]}] (pred all-tags))
   scenarios))

(defn get-scenarios-matching-all
  [scenarios and-tags]
  (get-scenarios-matching-pred
   scenarios
   (fn [all-tags]
     (= and-tags (intersection all-tags and-tags)))))

(defn get-scenarios-matching-any
  [scenarios or-tags]
  (get-scenarios-matching-pred
   scenarios
   (fn [all-tags]
     (seq (intersection all-tags or-tags)))))

(defn project-tags
  "Project the tags of feature parents to their scenario children. This makes
  tag-based matching easier."
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
  [{:keys [and-tags or-tags]} {:keys [scenarios] :as feature}]
  (let [matching-scenarios
        (cond
          (seq and-tags) (get-scenarios-matching-all scenarios and-tags)
          (seq or-tags) (get-scenarios-matching-any scenarios or-tags)
          :else scenarios)]
    (assoc feature :scenarios matching-scenarios)))

(defn get-features-to-run
  [features tags]
  (->> features
       (map project-tags)
       (map (partial remove-non-matching-scenarios tags))
  )


  )

(def registry (atom {}))

(defn register
  [step-type step-text step-fn]
  (swap! registry assoc-in [step-type step-text] step-fn))

(defn Given
  [step-text perform]
  (register :given step-text perform))

(defn When
  [step-text perform]
  (register :when step-text perform))

(defn Then
  [step-text perform]
  (register :then step-text perform))

(defn load-steps
  "Load step registries dynamically from files under dir-path and return a
  single registry map"
  [dir-path]
  {:given {"a monkey" (fn [a] 2)}})

(defn get-step-fns
  "Get the step functions in step-registry that match step. TODO: :text will be
  a regular expression in some cases; account for this!"
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

(defn run
  "Run seq of features matching tags using any steps discoverable under
  steps-path"
  [features tags steps-path]
  (let [features-to-run (get-features-to-run features tags)
        step-registry (load-steps steps-path)
        features-with-step-fns (add-step-fns features-to-run step-registry)]
    features-with-step-fns))
