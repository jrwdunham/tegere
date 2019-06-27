(ns tegere.loader-fiddle
  "Fiddle file for playing around with loader.clj."
  (:require [tegere.loader :refer :all]))

(def path-1 "/Users/joeldunham/Development/tegere/tegere/examples/apes/src")

(def path-2 "/Users/joeldunham/Development/tegere/tegere/examples/apes")

(comment

  (do
    (require '[tegere.steps :as tegsteps])
    (reset! tegsteps/registry {})
    (let [init-reg @tegsteps/registry]
      (load-clojure-source-files-under-path path-1)
      [init-reg @tegsteps/registry]))

  (do
    (require '[tegere.steps :as tegsteps])
    (reset! tegsteps/registry {})
    (let [init-reg @tegsteps/registry]
      (load-clojure-source-files-under-path path-2)
      [init-reg @tegsteps/registry]))

)
