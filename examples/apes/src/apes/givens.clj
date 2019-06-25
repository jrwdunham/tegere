(ns apes.givens
  (:require [clojure.string :as s]
            [tegere.steps :refer [Given When Then]]
            [apes.utils :refer [update-step-rets]]))

(Given "a {animal}"
       (fn [context animal]
         (update-step-rets
          context
          (->> animal
               (format "a-%s")
               keyword))))

(Given "everything is all good"
       (fn [context]
         (update-step-rets
          context
          :all-good)))
