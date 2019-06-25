(ns apes.steps.steps
  (:require [clojure.string :as s]
            [tegere.steps :refer [Given When Then]]
            [apes.steps.utils :refer [update-steps-rets]]))

(Given "a {animal}"
       (fn [context animal]
         (update-step-rets
          context
          (->> animal
               (format "a-%s")
               keyword))))

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

(When "I give him a {noun}"
      (fn [context noun]
        (update-step-rets
         context
         (->> noun
              (format "give-him-a-%s")
              keyword))))

(When "I present him with an orangutan"
      (fn [context]
        (update-step-rets
         context
         :present-with-orang)))
