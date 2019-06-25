(ns apes.steps.utils)

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
