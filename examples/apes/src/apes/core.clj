(ns apes.core
  "Gherkin features prescribing th behaviour of our cousins, the great apes."
  (:require #_[clojure.pprint :as pprint]
            [taoensso.timbre.appenders.core :as appenders]
            [taoensso.timbre :as timbre]
            [tegere.cli2 :as cli]
            [tegere.loader :as l]
            [tegere.runner :as r]
            [tegere.steps :as s]
            [apes.steps.core]
            [clojure.string :as str])
  (:gen-class))

(if-let [log-path (System/getenv "APES_LOG_FILE")]
  (timbre/merge-config!
   {:appenders
    {:spit (appenders/spit-appender {:fname log-path})
     :println {:enabled? false}}}))

(defn main
  [{:keys [::r/features-path] :as config}]
  (r/run
    (l/load-feature-files features-path)
    @s/registry
    config
    :initial-ctx {:config config}))

(def ^:dynamic *exit?* true)

(defn -main
  "Example usage:

      $ clj -m apes.core /src/apes/features/ --tags=bonobos --stop
  "
  [& args]
  (let [{:keys [exit-message ok?] :as config} (cli/validate-args args)]
    (if exit-message
      (do
        (println exit-message)
        (if *exit?* (System/exit (if ok? 0 1))))
      (main config))))

(comment

  (binding [*exit?* false]
    (-main
     "src/apes/features"
     "--stop"))

  (binding [*exit?* false]
    (-main "path"))

  (binding [*exit?* false]
    (-main
     "src/apes/features"
     "--tags=@bonobos or @chimpanzees"
     "-Durl=http://api.example.com"
     "--data=password=secret"
     "--stop"
     "--verbose"))

  (-main
   "src/apes/features"
   "--tags=cow,chicken"
   "--tags=not @a or @b and not @c or not @d or @e and @f"
   "-Durl=http://api.example.com"
   "--data=password=secret"
   "--stop"
   "--verbose")

  (-main "-vvvp8080" "foo" "--help" "--invalid-opt")

  (-main "--help")

  (-main "--stop")

  (-main "-Da=b" "-Dc=d" "--tags=chimpanzees")

  (-main "-Da=b" "-Dc=d" "--tags=dogs" "--tags=chimpanzees")

  (-main "-Da=b" "-Dc=d" "--stop" "--tags=dogs" "--tags=chimpanzees")

  (-main "path/to/features" "-Da=b" "-Dc=d" "--stop" "--tags=dogs" "--tags=chimpanzees")

)
