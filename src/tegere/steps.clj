(ns tegere.steps
  "Supplies Given, When and Then functions which register the supplied step
  text with the supplied perform function")

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
