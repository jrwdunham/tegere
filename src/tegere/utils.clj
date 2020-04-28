(ns tegere.utils
  "Reusable utilities for TeGere."
  (:require [clojure.pprint :as pprint]))

(defn pp-str
  [x]
  (with-out-str (pprint/pprint x)))

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

(defn just
  [x]
  [x nil])

(defn nothing
  [error]
  [nil error])

(defn maybe-reducer
  "Takes a reducing function and returns one that short-circuits on an accumulator
  argument that is a Nothing 2-vec, i.e., a vector with a non-nil second item."
  [f]
  (fn [[acc error] item]
    (if error
      (nothing error)
      (f acc item))))

(defn maybe-kv-reducer
  [f]
  (fn [[acc error] k v]
    (if error
      (nothing error)
      (f acc k v))))

(defn maybes->maybe
  "Convert a sequence of maybe Xs (2-vecs) to a single maybe sequence of Xs."
  [maybes]
  (let [first-error (->> maybes (map second) (filter some?) first)]
    (if first-error
      (nothing first-error)
      (just (map first maybes)))))

(defn just-then
  "Given a maybe (`[just nothing]`) and a just function that returns a value given
  the `just` as input, return that value, or a nothing (`[nil error]`) if
  `error` is truthy."
  ([maybe just-fn] (just-then maybe just-fn identity))
  ([[val error] just-fn nothing-fn]
   (if error
     (nothing (nothing-fn error))
     (just (just-fn val)))))

(defn just-then-esc
  ([maybe] (just-then-esc maybe identity identity))
  ([maybe just-fn] (just-then-esc maybe just-fn identity))
  ([[val error] just-fn nothing-fn]
   (if error
     (nothing-fn error)
     (just-fn val))))

(defn nothing-then
  "Just like just-then except the nothing handler function is the only one required."
  ([maybe nothing-fn] (nothing-then maybe nothing-fn identity))
  ([[val error] nothing-fn then-fn]
   (if error
     (nothing (nothing-fn error))
     (just (then-fn val)))))
