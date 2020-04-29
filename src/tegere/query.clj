(ns tegere.query
  "Functionality for querying a ``:tegere.parser/features`` collection, including
  preparing said collection for querying."
  (:require [alandipert.intension :refer [make-db]]
            [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [datascript.core :refer [q]]))

(s/def ::tag string?)
(s/def ::tags (s/coll-of ::tag))

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
      tags))
   features
   (get-all-scenario-tags features)))

