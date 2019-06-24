(ns tegere.loader
  "Functionality for loading feature and steps files."
  (:require [clojure.string :as s]))

(defn find-files-with-extensions
  "Return all files under root-path that have any of the file extensions in
  extensions."
  [root-path extensions]
  (->> root-path
       clojure.java.io/file
       file-seq
       (filter #(.isFile %))
       (map #(.toPath %))
       (filter #(some (-> % str (s/split #"\.") last vector set) extensions))))

(defn find-feature-files
  "Locate any feature files under root-path."
  [root-path]
  (find-files-with-extensions root-path ["feature"]))

(defn find-clojure-files
  "Locate any Clojure files under root-path."
  [root-path]
  (find-files-with-extensions root-path ["clj" "cljs" "cljc"]))

(defn path->registry
  "Given a path to a Clojure source file, load it and return a var named
  registry, if possible."
  [path]
  (try
    (-> path
        str
        load-file
        meta
        :ns
        ns-interns
        (get 'registry))
    (catch Exception e nil)))

(defn load-steps
  "Load step registries dynamically from files under root-path and return a
  single registry map."
  [root-path]
  (->> root-path
       find-clojure-files
       (map path->registry)
       (filter some?)
       (map deref)
       (apply (partial merge-with merge))))
