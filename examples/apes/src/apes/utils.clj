(ns apes.utils)

(defn update-step-rets
  "Convenience function that appends val to the :step-rets key of the map
  context, while ensuring that the val of :step-rets is a vec."
  [context val]
  (update
   context
   :step-rets
   (fn [step-rets]
     (if (seq step-rets)
       (conj step-rets val)
       [val]))))
