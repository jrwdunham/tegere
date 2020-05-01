(ns tegere.query
  "Functionality for querying a ``:tegere.parser/features`` collection, including
  preparing said collection for querying by 'projecting' tags from features and
  example tables to the scenarios that are the ultimate targets of queries."
  (:require [alandipert.intension :refer [make-db]]
            [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [datascript.core :refer [q]]))

(s/def ::tag string?)
(s/def ::tags (s/coll-of ::tag))
(s/def ::query-non-terminal #{'and 'or 'not})
(s/def ::query-terminal string?)
(s/def ::query-tree
  (s/cat
   :root ::query-non-terminal
   :branches (s/+ (s/or :leaf ::query-terminal
                        :branch ::query-tree))))

(defn features->db [features] (make-db features))

(def examples-query
  '[:find ?feat-idx ?scen-idx ?ex-k ?ex-v
    :where
    [?feat-idx :tegere.parser/scenarios ?scen-idx :tegere.parser/examples-row
     ?ex-k ?ex-v]])

(def tags-query
  '[:find ?feat-idx ?feat-tag ?scen-idx ?scen-tag
    :where
    [?feat-idx :tegere.parser/tags _ ?feat-tag]
    [?feat-idx :tegere.parser/scenarios ?scen-idx :tegere.parser/tags _
     ?scen-tag]])

(defn- get-examples [db] (q examples-query db))

(defn- get-tags [db] (q tags-query db))

(defn- example-row-processor [[feat-idx scen-idx row-key row-val]]
  {:path [feat-idx scen-idx]
   :tags #{(format "%s=%s" row-key row-val)}})

(defn- tag-row-processor [[feat-idx feat-tag scen-idx scen-tag]]
  {:path [feat-idx scen-idx]
   :tags (set [feat-tag scen-tag])})

(defn- set-union-tags [[path aggregated-tags]]
  [path (->> aggregated-tags
             (map :tags)
             (reduce set/union))])

(defn get-all-scenario-tags
  "Given a ``:tegere.parser/features`` vector, return a map from scenario paths
  within that vector (2-ary vecs of int) to sets of tags belonging to the
  scenario at the path. E.g.,
  {[0 0] #{manner_of_looking=quizzically chimpanzees fruit-reactions} ...}."
  [features]
  (let [db (features->db features)]
    (->> (concat (map example-row-processor (get-examples db))
                 (map tag-row-processor (get-tags db)))
         (group-by :path)
         (map set-union-tags)
         (into {}))))

(defn set-all-scenario-tags
  "Return a modified copy of the ``:tegere.parser/features`` vector ``features``
  such that each feature's ``:tegere.parser/scenario`` contains a ``::tags`` set
  containing *all* of the tags that pertain to that scenario."
  [features]
  (reduce
   (fn [features [[feat-idx scen-idx] tags]]
     (assoc-in
      features
      [feat-idx :tegere.parser/scenarios scen-idx ::tags]
      (vec tags)))
   features
   (get-all-scenario-tags features)))

(defn user-query->where-clause
  "Given ``::query-tree`` ``user-query``, return one or more datalog where
  clauses. If the user query is a conjunction ('and) at the top level, then the
  return value is a list of vector coordinands. If the user query is a
  disjunction ('or) or negation ('not), the return value is a list whose first
  element is that symbol. Otherwise, the return value is a vector."
  ([user-query] (user-query->where-clause user-query false))
  ([user-query keep-and?]
   (if (coll? user-query)
     (let [first-sym (symbol (name (first user-query)))
           branches (map (fn [p] (user-query->where-clause p true))
                         (rest user-query))]
       (if (or keep-and? (not= 'and first-sym))
         (conj branches first-sym)
         branches))
     ['?feat-idx '_ '?scen-idx :tegere.query/tags '_ user-query])))

(defn user-query->datalog-query
  "Given a user query conformant to ``::query-tree`` (e.g.,
  '(and chimpanzees bonobos)), return a datalog query that can be passed to
  ``datascript.core/q``. The return value of executing this query will be a set
  of 2-ary vectors containing a feature index and a scenario index."
  [user-query]
  (let [base '[:find ?feat-idx ?scen-idx
               :where
               [?feat-idx :tegere.parser/scenarios ?scen-idx]]
        where-clauses (user-query->where-clause user-query)]
    (if user-query
      (if (symbol? (first where-clauses))
        (conj base where-clauses)
        (vec (concat base where-clauses)))
      base)))

(defn- ->map-indices
  [indices]
  (->> indices
       (group-by first)
       (map (fn [[f-idx s-idxs]] [f-idx (mapv second s-idxs)]))
       (into {})))

(defn filter-scenarios [scenarios indices]
  (->> scenarios (keep-indexed (fn [idx s] (when (some #{idx} indices) s))) vec))

(defn filter-features [features indices]
  (let [indices (->map-indices indices)]
    (->> features
         (map-indexed
          (fn [idx f]
            (when-let [s-idcs (get indices idx)]
              (update f :tegere.parser/scenarios filter-scenarios s-idcs))))
         (filterv some?))))

(defn query
  "Query the features data structure (conformant to ``:tegere.parser/features``)
  using where clause ``where`` and returning a substructure of ``features`` that
  contains only the features and scenarios that match the where clause. The
  ``where`` clause must be a list-based tree structure that conforms to
  ``::query-tree``, i.e., its non-terminal nodes are the symbols 'and 'or or 'not
  and its terminal nodes are strings that should match Gherkin tags in the
  features. Example usage::

      (query features '(and (or chimpanzees orangutan) (not fruit-reactions)))"
  [features where]
  (let [features (set-all-scenario-tags features)]
    (->> features
         make-db
         (q (user-query->datalog-query where))
         (filter-features features))))
