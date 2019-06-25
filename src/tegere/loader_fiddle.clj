(ns tegere.loader-fiddle
  "Fiddle file for playing around with loader.clj."
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.tools.namespace.find :as ctn-find]
            [cemerick.pomegranate :as pom]
            [tegere.parser :refer [parse]]
            [tegere.loader :refer :all])
  (:import java.io.File)
  (:use [clojure.java.io :only (as-url)]))

(defn find-namespaces-in-dir
  [dir-path]
  (-> dir-path
      io/as-file
      ctn-find/find-ns-decls-in-dir
      seq))

(defn find-clojure-sources-in-dir
  [dir-path]
  (-> dir-path
      io/as-file
      ctn-find/find-clojure-sources-in-dir
      seq))

(defn require-namespaces-in-dir
  [dir-path]
  (when-let [namespaces-in-dir (find-namespaces-in-dir dir-path)]
    (pom/add-classpath dir-path)
    (doseq [dyn-ns (map second namespaces-in-dir)]
      (import dyn-ns)
      )
    ))

(defn load-clojure-sources-in-dir
  [dir-path]
  (when-let [clojure-sources-in-dir (find-clojure-sources-in-dir dir-path)]
    (pom/add-classpath dir-path)
    ;clojure-sources-in-dir
    (doseq [x clojure-sources-in-dir]
      (load-file (str x)))
    ))

(defn add-system-classpath
  "Add an url path to the system class loader"
  [url-string]
  (let [field (aget (.getDeclaredFields java.net.URLClassLoader) 0)]
    (.setAccessible field true)
    (let [ucp (.get field (ClassLoader/getSystemClassLoader))]
      ;(.addURL ucp (java.net.URL. url-string))
      (.addURL ucp (as-url (File. url-string)))
      )))

(defn xxx
  [dir-path]
  (when-let [clojure-sources-in-dir (find-clojure-sources-in-dir dir-path)]
    (add-system-classpath dir-path)
    (doseq [x clojure-sources-in-dir]
      ;(load-file (str x))
      (load-file (str x))
      )
    ))

;; TODO: this seems to work!
(defn yyy
  [dir-path]
  (when-let [namespaces-in-dir (find-namespaces-in-dir dir-path)]
    (add-system-classpath dir-path)
    (->> (.getURLs (java.lang.ClassLoader/getSystemClassLoader))
         (map str)
    )
    (doseq [dyn-ns (map second namespaces-in-dir)]
      (require dyn-ns)
    )
  )
)


;; This works
(def my-path "/Users/joeldunham/Development/tegere/tegere/examples/apes/src")

;; This does not work
(def my-other-path "/Users/joeldunham/Development/tegere/tegere/examples/apes")

(comment

  (* 8 8)

  (yyy my-path)

  (require '[tegere.steps :as tegsteps])

  tegsteps/registry

  (yyy my-other-path)

  (xxx my-path)

  (as-url  (File. "/tmp"))

  (java.net.URL. "abc")

  (let [dir-path
        "/Users/joeldunham/Development/tegere/tegere/examples/apes"]
    (add-system-classpath dir-path))

  (let [dir-path
        "/Users/joeldunham/Development/tegere/tegere/examples/apes"]
    (find-clojure-sources-in-dir dir-path))

  (let [dir-path
        "/Users/joeldunham/Development/tegere/tegere/examples/apes"]
    (load-clojure-sources-in-dir dir-path))

  (find-namespaces-in-dir "examples/apes")

  (pom/classloader-hierarchy)

  (do
    (pom/add-classpath
     "/Users/joeldunham/Development/tegere/tegere/examples/apes"
     (java.lang.ClassLoader/getSystemClassLoader))
    (->> (.getURLs (java.lang.ClassLoader/getSystemClassLoader))
         (map str)
         ;(filter #(s/includes? % "ape"))
    )
  )

  (let [dir-path
        "/Users/joeldunham/Development/tegere/tegere/examples/apes"
        classloaders (pom/classloader-hierarchy)]
    (if-let [cl (last (filter pom/modifiable-classloader? classloaders))]
      (do
        (pom/add-classpath
         dir-path
         cl)
        (->> (.getURLs cl)
             (map str)
             ;(filter #(s/includes? % "ape"))
        )
        (require-namespaces-in-dir dir-path)
      )
    )
  )

  (do
    (pom/add-classpath
     "/Users/joeldunham/Development/tegere/tegere/examples/apes"
     (java.lang.ClassLoader/getSystemClassLoader))
    (->> (.getURLs (java.lang.ClassLoader/getSystemClassLoader))
         (map str)
                                        ;(filter #(s/includes? % "ape"))
         )
    )

  (pom/add-classpath "examples/apes")

  (map str (.getURLs (java.lang.ClassLoader/getSystemClassLoader)))

  (require-namespaces-in-dir "examples/apes")

  (ctn-find/find-ns-decls-in-dir (io/as-file "examples/apes"))

  (->
   (ctn-find/find-ns-decls-in-dir (io/as-file "examples/apes"))
   first
   second
   )

  (* 8 8)

  (let [fs (slurp "examples/apes/features/monkey-behaviour.feature")]
    (parse fs)
    ;(str fs)
  )

  (let [x ["/path/"]]
    (-> x
        first
        (or ".")
        ))

  (find-feature-files "examples/apes")

  (find-clojure-files "examples/apes")

  (load-steps "src/examples/apes")

  (->> (load-steps "examples/apes")
       vals
       (map keys)
       flatten
       set)

  (let [r (load-steps "examples/apes")
        f1 (-> r :given (get "a setup"))
        f2 (-> r :when (get "an action"))
        f3 (-> r :then (get "a result"))]
    [(f1 {}) (f2 {}) (f3 {})])

)
