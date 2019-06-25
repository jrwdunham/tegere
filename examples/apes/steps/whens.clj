(ns apes.steps.whens
  (:require [clojure.string :as s]
            [tegere.steps :refer [Given When Then]]
            [apes.steps.utils :refer [update-steps-rets]]))

(When "an action"
       (fn [context]
         (update-step-rets context :an-action)))

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
