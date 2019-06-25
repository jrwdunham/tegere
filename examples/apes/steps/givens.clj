(ns apes.steps.givens
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
