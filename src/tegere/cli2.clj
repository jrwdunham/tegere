(ns tegere.cli2
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as str]))

(defn- update-with-conj [opts id val]
  (update opts id conj val))

(defn- update-with-merge [opts id val]
  (update opts id merge val))

(defn- parse-data [data]
  (let [[k v] (str/split data #"=" 2)]
    {(keyword k) v}))

(defn- split-if-commas [s]
  (let [[x & y :as z] (str/split s #",\s*")]
    (if y z x)))

(def cli-options
  [["-h" "--help"]
   ["-s" "--stop" :default false]
   ["-v" "--verbose" :default false]
   ["-t" "--tags TAGS" "Tags to control which features are executed"
    :assoc-fn update-with-conj
    :parse-fn split-if-commas]
   ["-D" "--data KEYVAL" "Data in key=val format to pass to Apes Gherkin"
    :assoc-fn update-with-merge
    :parse-fn parse-data]])

(defn parse-opts [args]
  (cli/parse-opts args cli-options))

(comment

  (parse-opts ["--help"])

  (parse-opts ["--stop"])

  (parse-opts ["-Da=b" "-Dc=d" "--tags=chimpanzees"])

  (parse-opts ["-Da=b" "-Dc=d" "--tags=dogs" "--tags=chimpanzees"])

  (parse-opts ["-Da=b" "-Dc=d" "--stop" "--tags=dogs" "--tags=chimpanzees"])

  (parse-opts ["path/to/features" "-Da=b" "-Dc=d" "--stop" "--tags=dogs" "--tags=chimpanzees"])

  (parse-opts ["path/to/features" "-Da=b" "-Dc=d" "--stop" "--tags=dogs" "--tags=chimpanzees"
               "--tags=a,b,c"])

)
