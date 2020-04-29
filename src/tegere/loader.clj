(ns tegere.loader
  "Functionality for loading feature files."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [tegere.parser :as parser]
            #_[tegere.utils :as u])
  (:import java.io.File))

(defn find-files-with-extensions
  "Return all files under root-path that have any of the file extensions in
  extensions."
  [root-path extensions]
  (->> root-path
       io/file
       file-seq
       (filter #(.isFile %))
       (map #(.toPath %))
       (filter #(some (-> % str (str/split #"\.") last vector set) extensions))))

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
