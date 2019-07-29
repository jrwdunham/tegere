(ns tegere.utils
  "Reusable utilities for TeGere.")

(defn bind
  "Call f on val if err is nil, otherwise return [nil err]
  See https://adambard.com/blog/acceptable-error-handling-in-clojure/."
  [f [val err]]
  (if (nil? err)
    (f val)
    [nil err]))

(defmacro err->>
  "Thread-last val through all fns, each wrapped in bind.
  See https://adambard.com/blog/acceptable-error-handling-in-clojure/."
  [val & fns]
  (let [fns (for [f fns] `(bind ~f))]
    `(->> [~val nil]
          ~@fns)))

(defn deep-merge-with
  "Like merge-with, but merges maps recursively, applying the given fn
  only when there's a non-map at a particular level.
  (deep-merge-with + {:a {:b {:c 1 :d {:x 1 :y 2}} :e 3} :f 4}
                     {:a {:b {:c 2 :d {:z 9} :z 3} :e 100}})
  -> {:a {:b {:z 3, :c 3, :d {:z 9, :x 1, :y 2}}, :e 103}, :f 4}
  Taken from https://clojuredocs.org/clojure.core/merge-with."
  [f & maps]
  (apply
   (fn m [& maps]
     (if (every? map? maps)
       (apply merge-with m maps)
       (apply f maps)))
   maps))
