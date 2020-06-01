(ns tegere.loader
  "Functionality for loading feature files."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [tegere.parser :as parser])
  (:import java.io.File))

(defn- is-file? [^java.io.File f] (.isFile f))

(defn- file->path-str [^java.io.File f] (str (.toPath f)))

(defn- get-extension [^String p] (-> p (str/split #"\.") last))

(defn- extension-in? [extensions ^String path]
  (some (-> path get-extension vector set) extensions))

(defn find-files-with-extensions
  "Return all files under root-path that have any of the file extensions in
  extensions."
  [root-path extensions]
  (->> root-path
       io/file
       file-seq
       (filter is-file?)
       (map file->path-str)
       (filter (partial extension-in? extensions))))

(defn find-feature-files
  "Locate any feature files under root-path."
  [root-path]
  (find-files-with-extensions root-path ["feature"]))

(defn load-feature-files
  [root-path]
  (->> root-path
       find-feature-files
       (mapv (comp parser/parse slurp str))
       #_u/maybes->maybe))
