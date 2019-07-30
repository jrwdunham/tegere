(ns tegere.loader
  "Functionality for loading feature and steps files."
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.tools.namespace.find :as ctn-find]
            [me.raynes.fs :as fs]
            [tegere.parser :refer [parse]])
  (:import java.io.File)
  (:use [clojure.java.io :only (as-url)]))

(defn add-to-system-classpath
  "Add a path (string) to the system class loader. See
  https://groups.google.com/forum/#!topic/clojure/Gl2fVryBqBg. Note: this breaks
  ``lein test``, which uses a different classpath (?)"
  [dir-path]
  (let [field (aget (.getDeclaredFields java.net.URLClassLoader) 0)]
    (.setAccessible field true)
    (let [ucp (.get field (ClassLoader/getSystemClassLoader))]
      (.addURL ucp (as-url (File. dir-path))))))

(defn find-clojure-sources-in-dir
  "Return a seq of Clojure source files under dir-path or nil if there are none.
  Minimal wrapper around
  clojure.tools.namespace.find/find-clojure-sources-in-dir."
  [dir-path]
  (-> dir-path
      io/as-file
      ctn-find/find-clojure-sources-in-dir
      seq))

(defn find-namespaces-in-dir
  "Return a seq of namespace sexps that are defined in any Clojure files under
  dir-path."
  [dir-path]
  (-> dir-path
      io/as-file
      ctn-find/find-ns-decls-in-dir
      seq))

(defn find-namespace-strings-in-dir
  "Return a seq of namespace names as strings from any Clojure files under
  dir-path."
  [dir-path]
  (->> dir-path
       find-namespaces-in-dir
       (map (comp str second))))

(defn namespace->path-replacer
  "Given a namespace string, return a regex that will match the end of the
  filesystem file path wherein that namespace should be defined. E.g.,
  'my-namespace.core' should return #'my_namespace/core$'."
  [ns-str]
  (-> ns-str
      (s/replace #"-" "_")
      (s/replace #"\." java.io.File/separator)
      (#(format "%s$" %))
      re-pattern))

(defn get-needed-classpath
  "Given a Clojure source file object, return a string representing the
  classpath that is needed in order to call load-file on that file object.
  This analyzes the file object's path and the namespace defined within it in
  order to do this."
  [clj-src-file-obj]
  (let [path-no-ext (-> clj-src-file-obj str (s/replace #"\.clj(c)?$" ""))
        replacer (->> clj-src-file-obj
                      fs/parent
                      str
                      find-namespace-strings-in-dir
                      (map namespace->path-replacer)
                      flatten
                      set
                      (filter (fn [p] (re-find p path-no-ext)))
                      first)]
    (if replacer (s/replace path-no-ext replacer "") nil)))

(defn get-classpath-additions
  "Given a seq of Clojure source files (paths), return a set of paths (strings)
  that must be added to the system classpath in order for these files to be
  loadable."
  [clojure-sources-in-dir]
  (->> clojure-sources-in-dir
       (map get-needed-classpath)
       set))

(defn load-clojure-source-files-under-path
  "Load all Clojure source files under dir-path, after making the necessary
  modifications to the system classpath."
  [dir-path]
  (when-let [clojure-sources-in-dir (find-clojure-sources-in-dir dir-path)]
    (doseq [classpath-addition (get-classpath-additions clojure-sources-in-dir)]
      (add-to-system-classpath classpath-addition))
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

(defn load-feature-files
  [root-path]
  (->> root-path
       find-feature-files
       (map (comp parse slurp str))))
