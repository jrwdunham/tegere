(ns tegere.print-fiddle
  (:require [tegere.print :as p]))

(def outcome-map
  {{:name "Monkeys behave as expected"
    :description
    "Experimenters want to ensure that their monkey simulations are behaving correctly."
    :tags (list "monkeys")}
   {{:description
     "Monkeys behave as expected when offered various foods.",
     :tags (list "fruit-reactions")}
    {:error 2, :fail 2}}})

(def outcome-map-2
  {
   {:name "Monkeys behave as expected"
    :description
    "Experimenters want to ensure that their monkey simulations are behaving correctly."
    :tags (list "monkeys")}
   {{:description
     "Monkeys behave as expected when offered various foods.",
     :tags (list "fruit-reactions")}
    {:error 2, :fail 2}}

   {:name "ZZZ-Monkeys behave as expected"
     :description
     "ZZZ-Experimenters want to ensure that their monkey simulations are behaving correctly."
     :tags (list "monkeys")}
    {{:description
      "ZZZ-Monkeys behave as expected when offered various foods.",
      :tags (list "fruit-reactions")}
     {:error 2, :fail 2}}

   {:name "AAA-Monkeys behave as expected"
    :description
    "AAA-Experimenters want to ensure that their monkey simulations are behaving correctly."
    :tags (list "monkeys")}
   {{:description
     "AAA-Monkeys behave as expected when offered various foods.",
     :tags (list "fruit-reactions")}
    {:pass 4 :error 0, :fail 0}}

   })

(defn deep-merge-with
  "Like merge-with, but merges maps recursively, applying the given fn
  only when there's a non-map at a particular level.
  (deep-merge-with + {:a {:b {:c 1 :d {:x 1 :y 2}} :e 3} :f 4}
                     {:a {:b {:c 2 :d {:z 9} :z 3} :e 100}})
  -> {:a {:b {:z 3, :c 3, :d {:z 9, :x 1, :y 2}}, :e 103}, :f 4}"
  [f & maps]
  (apply
   (fn m [& maps]
     (if (every? map? maps)
       (apply merge-with m maps)
       (apply f maps)))
   maps))

(defn do-the-thing
  [agg [_ scenarios]]
  (let [steps
        (apply (partial merge-with + {:passed 0 :failed 0})
               (->> scenarios
                    vals
                    (map (fn [scen-val]
                           (reduce
                            (fn [agg [k v]]
                              (let [new-k (if (some #{k} [:error :fail])
                                            :failed :passed)]
                                (merge-with + agg {new-k v})))
                            {:passed 0 :failed 0}
                            scen-val)))))
        scenarios
        (reduce (fn [agg [_ s-o]]
                  (let [scen-failed
                        (->> s-o
                             ((juxt :error :fail))
                             (filter int?)
                             (reduce +)
                             (< 0))
                        rslt (if scen-failed {:failed 1} {:passed 1})]
                    (merge-with + agg rslt)))
                {:passed 0 :failed 0}
                scenarios)
        features (if (> (:failed scenarios) 0)
                   {:failed 1} {:passed 1})]
    (deep-merge-with + agg
                     {:features features}
                     {:scenarios scenarios}
                     {:steps steps})))

(comment

  (deep-merge-with + {:a {:b 1}} {:a {:b 2}})

  (some #{6} (list 1 2))

  (reduce do-the-thing
          {:features {:passed 0 :failed 0}
           :scenarios {:passed 0 :failed 0}
           :steps {:passed 0 :failed 0}}
          outcome-map-2)

  (->> {:error 2 :fail 2}
       ((juxt :error :fail))
       (filter int?)
       (reduce +)
       (< 0))

  (->> 4 (< 0))

  (->> 0 (< 0))

  ((juxt :error :zfail) {:error 2 :fail 2})

  (filter int? [0 false 20 2 0])

  (vals {:a 2 :b 4})

  (merge-with + {:a 1} {:a 2 :b 3})

  (apply (partial merge-with + {:a 0 :b 0 :c 0}) (list {:a 1} {:a 2 :b 3}))

  (p/style "dog" :red)

  (->> [{:a 2} {:b 44} {:a 22}]
       (map :a)
       (filter some?)
       last)

  (update-in {} [:a :b] (fn [old] [old :monkeys]))

  (update-in {} [:a :b :c] (fn [old] (if old (inc old) 1)))

  (update-in {:a {:b {:c 22}}} [:a :b :c] (fn [old] (if old (inc old) 1)))

  ((juxt :a :b :c) {:a 2 :b 3 :c 4})

  (map (fn [[a b]] [a b]) {:a :b :c :d})

  (reduce (fn [agg [k v]] (conj agg [k v])) [] {:a :b :c :d})

)

;; :feature
;; :scenario
