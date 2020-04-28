(ns apes.core
  "Gherkin features prescribing th behaviour of our cousins, the great apes."
  (:require #_[clojure.pprint :as pprint]
            [taoensso.timbre.appenders.core :as appenders]
            [taoensso.timbre :as timbre]
            [tegere.cli2 :as cli]
            [tegere.loader :as tegload]
            [tegere.runner :as tegrun]
            [tegere.steps :as tegstep]
            [apes.steps.core]
            [clojure.string :as str])
  (:gen-class))

(if-let [log-path (System/getenv "APES_LOG_FILE")]
  (timbre/merge-config!
   {:appenders
    {:spit (appenders/spit-appender {:fname log-path})
     :println {:enabled? false}}}))

(def default-features-path "src/apes/features")

(defn run-good
  "Load the Gherkin feature files under (::features-path config) and run
  them using the mappings from step statements to Clojure functions encoded in
  the `tegstep/registry` atom."
  ([] (run-good {}))
  ([config]
   (let [features (tegload/load-feature-files (::features-path config))]
     (tegrun/run
       features
       @tegstep/registry
       config
       :initial-ctx {:config config}))))

(defn run [config]
  config)

(defn main
  [{{:keys [tags stop data verbose]} :options args :arguments :as opts}]
  (run
    {::tags tags
     ::stop stop
     ::data data
     ::verbose verbose
     ::features-path (-> args first (or default-features-path))}))

(defn -main
  "Example usage:

      $ clj -m apes.core /src/apes/features/ --tags=bonobos --stop
  "
  [& args]
  (main (cli/parse-opts args)))


(comment

  (* 8 8)

  -main

  (-main "-vvvp8080" "foo" "--help" "--invalid-opt")

  (-main "--help")

  (-main "--stop")

  (-main "-Da=b" "-Dc=d" "--tags=chimpanzees")

  (-main "-Da=b" "-Dc=d" "--tags=dogs" "--tags=chimpanzees")

  (-main "-Da=b" "-Dc=d" "--stop" "--tags=dogs" "--tags=chimpanzees")

  (-main "path/to/features" "-Da=b" "-Dc=d" "--stop" "--tags=dogs" "--tags=chimpanzees")

  (cli/parse-opts
   ["path/to/features"
    "-Da=b"
    "-Dc=d"
    "--stop"
    "--tags=dogs"
    "--tags=chimpanzees"
    "--tags=c,d"])

)
