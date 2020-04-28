(ns tegere.print
  (:require [clojure.string :as s]
            [tegere.ansi :as tegansi]
            [tegere.parser :as p]))

(defn get-step-duration
  "Return seconds taken to complete step"
  [start-time end-time]
  (/ (- (.getTime end-time) (.getTime start-time)) 1000.0))

(defn get-step-type
  [step]
  (get step ::p/original-type (::p/type step)))

(defn get-step-repr
  [step start-time end-time]
  (format "    %s %s (took %ss)"
          (-> step
              get-step-type
              name
              s/capitalize)
          (::p/text step)
          (get-step-duration start-time end-time)))

(defn format-step
  [step start-time end-time color]
  (-> step
      (get-step-repr start-time end-time)
      (tegansi/style color)))

(defn format-step-passed 
  [step start-time end-time]
  (format-step step start-time end-time :green))

(defn format-step-errored
  [step start-time end-time]
  (format-step step start-time end-time :red))

(defn get-formatted-stack-trace
  "Given a stack trace as a seq of strings, return a formatted (indented and
  possibly colored) stack trace string."
  [stack-trace]
  (format "        %s" (s/join "\n        " stack-trace)))

(defn format-assertion-error
  "An assertion error is often multi-line (if it has a coder-specified message)
  so we want to format it correctly."
  [err-str]
  (let [parts (s/split err-str #"\n")]
    (s/join "\n            " parts)))

(defn print-step-fail
  [step err start-time end-time]
  (println (format-step step start-time end-time :yellow))
  (println (tegansi/style
            (str "        Assertion error: "
                 (format-assertion-error (:message err)))
            :yellow)))

(defn print-step-error
  [step err start-time end-time]
  (let [fmtd-stack-trace (get-formatted-stack-trace (:stack-trace err))]
    (println (format-step step start-time end-time :red))
    (println (tegansi/style (str "        Error: " (:message err)) :red))
    (println fmtd-stack-trace)))

(defn print-execution
  "Print a record of the execution of step, including duration and any failures
  or errors with stack traces."
  [step err start-time end-time]
  (cond
    (= :error (:type err)) (print-step-error step err start-time end-time)
    (= :fail (:type err)) (print-step-fail step err start-time end-time)
    :else (println (format-step-passed step start-time end-time))))

(defn get-feature-repr
  [feature]
  (format "\n%s%s %s\n  %s\n"
          (if (::p/tags feature)
            (tegansi/style (str "@" (s/join " @" (::p/tags feature)) "\n") :cyan)
            "")
          (tegansi/style "Feature:" :magenta)
          (::p/name feature)
          (::p/description feature)))

(defn print-feature
  [feature]
  (println (get-feature-repr feature)))

(defn get-scenario-repr
  [scenario]
  (format "\n%s  %s %s"
          (if (::p/tags scenario)
            (tegansi/style (str "  @" (s/join " @" (::p/tags scenario)) "\n") :cyan)
            "")
          (tegansi/style "Scenario:" :magenta)
          (::p/description scenario)))

(defn print-scenario
  [scenario]
  (println (get-scenario-repr scenario)))
