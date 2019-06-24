(ns examples.apes.steps.bonobos)

(def registry
  {:given {"a setup" (fn [ctx] :a-bonobo-setup)}
   :then {"a result" (fn [ctx] :a-result)}})
