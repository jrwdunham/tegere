(ns tegere.cli
  "A simple command-line interface that may be used by a TeGere-using project.
  Defines validate-args which can convert prescriptively valid command-line
  arguments into a ``:tegere.runner/config`` config map."
  (:require [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [me.raynes.fs :as fs]
            [tegere.grammar :refer [old-style-tag-expr-prsr]]
            [tegere.parser :as p]
            [tegere.runner :as r]
            [tegere.query :as q]))

(defn- update-with-merge [opts id val]
  (update opts id merge val))

(defn- parse-data [data]
  (let [[k v] (str/split data #"=" 2)]
    {(keyword k) v}))

(defn conjoin-tag-expression
  "Conjoin ``val`` to existing ``::q/query-tree`` at ``id`` of ``opts`` using
  ``'and``."
  [opts id val]
  (assoc opts id
         (if-let [existing-query-tree (id opts)]
           (list 'and val existing-query-tree)
           val)))

(def cli-options
  [["-h" "--help"]
   ["-s" "--stop" :default false :id ::r/stop]
   ["-v" "--verbose" :default false :id ::r/verbose]
   ;; Transform --tags values into a ::q/query-tree for datascript-based query.
   ["-t" "--tags TAGS" "Tags to control which features are executed"
    :assoc-fn conjoin-tag-expression
    :parse-fn p/parse-tag-expression-with-fallback
    :id ::q/query-tree]
   ["-D" "--data KEYVAL" "Data in key=val format to pass to Apes Gherkin"
    :assoc-fn update-with-merge
    :parse-fn parse-data
    :id ::r/data]])

(defn usage [options-summary]
  (->> ["TeGere Runner."
        ""
        "Usage: tegere-runner [options] features-path"
        ""
        "Options:"
        options-summary
        ""
        "features-path: path to directory with Gherkin feature files."]
       (str/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn features-path-exists?
  [features-path]
  (and features-path (fs/directory? features-path)))

(def default-config
  {::r/stop false
   ::r/verbose false
   ::r/data {}
   ::q/query-tree nil
   ::r/features-path "."})

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a
  ``:tegere.runner/config`` config map."
  ([args] (validate-args args cli-options))
  ([args cli-options]
   (let [{:keys [options arguments errors summary]}
         (cli/parse-opts args cli-options)
         config (merge default-config
                       {::r/features-path (or (first arguments) ".")}
                       options)]
     (cond
       (:help options) ; help => exit OK with usage summary
       {:exit-message (usage summary) :ok? true}
       errors ; errors => exit with description of errors
       {:exit-message (error-msg errors)}
       (features-path-exists? (::r/features-path config))
       config
       :else ; failed custom validation => exit with usage summary
       {:exit-message (format "There is no directory at path %s."
                              (::r/features-path config))}))))

(comment

  (validate-args ["--help"])

  (validate-args ["blargon"])

  (validate-args ["examples/apes/src/apes/features" "--stop"])

  (validate-args
   ["examples/apes/src/apes/features" "-Durl=http://www.url.com"
    "-Dpassword=1234" "--tags=chimpanzees"])

  (validate-args
   ["-Durl=http://www.url.com" "-Dpassword=1234" "--tags=dogs" "--tags=chimpanzees"
    "examples/apes/src/apes/features"])

  (validate-args
   ["-Durl=http://www.url.com" "-Dpassword=1234" "--stop" "--tags=dogs"
    "--tags=chimpanzees" "examples/apes/src/apes/features"])

  (validate-args
   ["examples/apes/src/apes/features" "-Durl=http://www.url.com"
    "-Dpassword=1234" "--stop" "--tags=dogs" "--tags=chimpanzees"])

  (validate-args
   ["examples/apes/src/apes/features" "-Durl=http://www.url.com"
    "-Dpassword=1234" "--stop" "--tags=dogs" "--tags=chimpanzees"
    "--tags=apple,orange,papaya"])

  (validate-args
   ["examples/apes/src/apes/features" "-Durl=http://www.url.com"
    "-Dpassword=1234" "--stop" "--tags=dogs" "--tags=chimpanzees"
    "--tags=apple,orange,papaya" "--tags=apple,orange,banana"])

  (old-style-tag-expr-prsr "cat,~@dog,cow,~bunny")

  (= '(and (not "ant")
           (and "rat"
                (or "cat" (not "dog") "cow" (not "bunny"))))
     (-> (validate-args ["examples/apes/src/apes/features"
                         "--tags=cat,~@dog,cow,~bunny"
                         "--tags=rat"
                         "--tags=~@ant"
                         ])
         ::q/query-tree))

  (-> (validate-args
       ["examples/apes/src/apes/features"
        "--tags=cow,chicken"
        "--tags=not @a or @b and not @c or not @d or @e and @f"])
      ::q/query-tree)

  (validate-args
   ["examples/apes/src/apes/features"
    "--tags=cow,chicken"
    "--tags=not @a or @b and not @c or not @d or @e and @f"
    "-Durl=http://api.example.com"
    "--data=password=secret"
    "--stop"
    "--verbose"])

)
