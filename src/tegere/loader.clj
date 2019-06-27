(ns tegere.loader
  "Functionality for loading feature and steps files."
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.tools.namespace.find :as ctn-find]
            [tegere.parser :refer [parse]]
            [tegere.steps :refer [registry]])
  (:import java.io.File)
  (:use [clojure.java.io :only (as-url)]))

(defn add-to-system-classpath
  "Add a path (string) to the system class loader"
  [dir-path]
  (let [field (aget (.getDeclaredFields java.net.URLClassLoader) 0)]
    (.setAccessible field true)
    (let [ucp (.get field (ClassLoader/getSystemClassLoader))]
      (.addURL ucp (as-url (File. dir-path))))))

(defn find-clojure-sources-in-dir
  "Return a seq of Clojure source files under dir-path."
  [dir-path]
  (-> dir-path
      io/as-file
      ctn-find/find-clojure-sources-in-dir
      seq))

(defn load-clojure-source-files-under-path
  "Load all Clojure source files under dir-path after adding dir-path to the
  system classpath."
  [dir-path]
  (when-let [clojure-sources-in-dir (find-clojure-sources-in-dir dir-path)]
    (add-to-system-classpath dir-path)
    (doseq [clojure-source-file clojure-sources-in-dir]
      (load-file (str clojure-source-file)))))

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

(defn load-steps-old
  "Load step registries dynamically from files under root-path and return a
  single registry map."
  [root-path]
  (->> root-path
       find-clojure-files
       (map path->registry)
       (filter some?)
       (map deref)
       (apply (partial merge-with merge))))

(defn load-steps-old-2
  "Load step registries dynamically from files under root-path and return a
  single registry map."
  [root-path]
  (doseq [path (find-clojure-files root-path)]
    (-> path str load-file))
  @registry)

(defn load-steps
  "Load step registries dynamically from files under root-path and return a
  single registry map."
  [root-path]
  (doseq [path (find-clojure-files root-path)]
    (-> path str load-file))
  @registry)

(defn load-feature-files
  [root-path]
  (->> root-path
       find-feature-files
       (map (comp parse slurp str))))
