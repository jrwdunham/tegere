(ns apes.steps.thens
  (:require [clojure.string :as s]
            [tegere.steps :refer [Then]]
            [apes.steps.utils :refer [update-steps-rets]]))

(Then "a result"
      (fn [context] (update-step-rets context :a-result)))

(Then "he doesn't eat it"
      (fn [context] (update-step-rets context :not-eat-it)))

(Then "he is {adjective}"
      (fn [context adjective]
        (update-step-rets
         context
         (->> adjective
              (format "is-%s")
              ((fn [string] (s/replace string #"\s+" "-")))
              keyword))))

(Then "he looks at me {adverb}"
      (fn [context adverb]
        (update-step-rets
         context
         (->> adverb
              (format "look-at-me-%s")
              keyword))))
