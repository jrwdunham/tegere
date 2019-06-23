(ns tegere.runner-fiddle
  "Fiddle file for playing around with runner.clj."
  (:require [clojure.string :as s]
            [clojure.set :refer [intersection]]
            [tegere.runner :refer :all]
            [tegere.parser :refer [parse]]
            [tegere.grammar-fiddle :refer [monkey-feature]]))

(defn update-step-rets
  "Convenience fiddle function that appends val to the :step-rets key of the map
  context, while ensuring that the val of :step-rets is a vec."
  [context val]
  (update-in
   context
   [:step-rets]
   (fn [step-rets]
     (if (seq step-rets)
       (conj step-rets val)
       [val]))))

(comment

  (let [features [(parse monkey-feature) (parse monkey-feature)]
        config {:tags {:and-tags #{"monkeys" "fruit-reactions"}}
                :stop false}
        fake-registry
        {:given {"a monkey" (fn [context] (update-step-rets context :a-monkey))}
         :when {"I give him a banana" (fn [context] (update-step-rets context :give-banana))
                "I give him a pear" (fn [context] (update-step-rets context :give-pear))}
         :then {"he doesn't eat it" (fn [context] (update-step-rets context :not-eat))
                ;"he is happy" (fn [context] (update-step-rets context :is-happy))
                "he is happy" (fn [context] (update-step-rets context (/ 1 0)))
                "he is sad" (fn [context] (update-step-rets context :is-sad))
                "he looks at me loathingly"
                (fn [context] (update-step-rets context :looks-loathingly))
                "he looks at me quizically"
                (fn [context] (update-step-rets context :looks-quizically))}}]
    (run features fake-registry config))

  (let [features [(parse monkey-feature) (parse monkey-feature)]
        config {:tags {:and-tags #{"monkeys" "fruit-reactions"}}
                :stop false}
        fake-registry
        {:given {"a monkey" (fn [context] (update-step-rets context :a-monkey))}
         :when {"I give him a {fruit}"
                (fn [context fruit]
                  (update-step-rets context (keyword (format "give-%s" fruit))))}
         :then {"he doesn't eat it" (fn [context] (update-step-rets context :not-eat))
                ;"he is happy" (fn [context] (update-step-rets context :is-happy))
                "he is happy" (fn [context] (update-step-rets context (/ 1 0)))
                "he is sad" (fn [context] (update-step-rets context :is-sad))
                "he looks at me loathingly"
                (fn [context] (update-step-rets context :looks-loathingly))
                "he looks at me quizically"
                (fn [context] (update-step-rets context :looks-quizically))}}]
    (run features fake-registry config))

    (keyword "a-banana")

)
