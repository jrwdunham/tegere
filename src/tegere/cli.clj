(ns tegere.cli
  "The Command-Line Interface (CLI) for TeGere. What allows you to call
  `tegere --tags=chimpanzee --stop` from a command line."
  (:require [clojure.string :as s]))

(defn split-by-comma [string] (s/split string #",\s*"))

(defn l-trim-hyph
  "Left-trim hyphens: '--stop-thing-' => 'stop-thing-'"
  [string]
  (-> string
      (s/replace #"^-+(.*)$" "$1")))

(defn format-arg-val-by-name
  [arg-val arg-name]
  (if (true? arg-val)
    arg-val
    (if (= arg-name :tags)
      (set (split-by-comma arg-val))
      arg-val)))

(defn format-arg-val-by-name-old
  [arg-val arg-name]
  (if (true? arg-val)
    arg-val
    (if (= arg-name :tags)
      (let [or-parts (split-by-comma arg-val)
            [_ & rest-or-parts] or-parts]
        (if rest-or-parts or-parts arg-val))
      arg-val)))

(defn parse-arg-name
  [arg-name]
  (if (s/starts-with? arg-name "-")
    [:kwargs (-> arg-name l-trim-hyph keyword)]
    [:args arg-name]))

(defn parse-arg
  "Parse str arg into 2-vec of attribute and value.
  Supports 'arg', '--opt=val', '-opt=val', and '--flag' type arguments."
  [arg]
  (let [raw (s/split arg #"=")
        [arg-type arg-name] (parse-arg-name (first raw))]
    [arg-type arg-name
     (-> raw second (or true) (format-arg-val-by-name arg-name))]))

(defn vectorize-merger
  "Assoc arg-val to agg under arg-name. If arg-name was already present, put the
  old value and the new value into a vector as the value."
  [agg arg-name arg-val]
  (assoc agg
         arg-name
         (if-let [existing-val (arg-name agg)]
           (->> arg-val (conj [existing-val]) flatten (apply vector))
           arg-val)))

(defn set-union-merge
  [agg arg-name arg-vals]
  (assoc agg
         arg-name
         (if-let [existing-val (get agg arg-name)]
           (set (concat existing-val arg-vals))
           arg-vals)))

(defn or-merge
  [agg arg-name arg-vals]
  (let [or-arg-name (->> arg-name name (format "or-%s") keyword)]
    (set-union-merge agg or-arg-name arg-vals)))

(defn and-merge
  [agg arg-name arg-vals]
  (let [and-arg-name (->> arg-name name (format "and-%s") keyword)]
    (set-union-merge agg and-arg-name arg-vals)))

(defn and-or-merger
  "Set a variant of arg-name on map agg to a value computed using arg-val."
  [agg arg-name arg-val]
  (if (> (count arg-val) 1)
    (or-merge agg arg-name arg-val)
    (and-merge agg arg-name arg-val)))

;; Map special argument names to special merger functions. The "tags" argument
;; is special in TeGere.
(def mergers-by-arg-name
  {:tags and-or-merger})

(defn add-arg-to-agg
  [agg arg]
  (let [[arg-type arg-name arg-val] (parse-arg arg)
        merger (get mergers-by-arg-name arg-name vectorize-merger)]
    (if (= arg-type :args)
      (update agg :args conj arg-name)
      (update agg :kwargs merger arg-name arg-val))))

(defn simple-cli-parser
  "Parse seq of strings to dict of keyword-arguments. This:

      arg --opt=val -opt-2=val --flag

  becomes:

      {arg true, opt val, opt-2 val, flag true}
  "
  [args]
  (reduce add-arg-to-agg {:args [] :kwargs {}} args))
