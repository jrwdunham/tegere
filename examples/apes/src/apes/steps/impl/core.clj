(ns apes.steps.impl.core
  (:require [clojure.string :as str]
            [apes.utils :refer [update-step-rets]]))

(defn a-result [context]
  (update-step-rets context :a-result))

(defn not-eat-it [context]
  (update-step-rets context :not-eat-it))

(defn is-adjective [context adjective]
  (update-step-rets
   context
   (->> adjective
        (format "is-%s")
        ((fn [string] (str/replace string #"\s+" "-")))
        keyword)))

(defn looks-at-me [context adverb]
  (update-step-rets
   context
   (->> adverb
        (format "look-at-me-%s")
        keyword)))

(defn an-action [context]
  (update-step-rets context :an-action))

(defn give-thing [context noun]
  (update-step-rets
   context
   (->> noun
        (format "give-him-a-%s")
        keyword)))

(defn present-orangutan [context]
  (update-step-rets
   context
   :present-with-orang))

(defn an-animal
  [context animal]
  (update-step-rets
   context
   (->> animal
        (format "a-%s")
        keyword)))

(defn all-good [context]
  (update-step-rets
   context
   :all-good))
