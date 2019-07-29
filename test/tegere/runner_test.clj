(ns tegere.runner-test
  (:require [clojure.test :as t]
            [tegere.runner :as sut]
            [tegere.parser :refer [parse]]))

;; A feature file (string) to use for testing
(def monkey-feature
  (str
   "# This is a comment about this feature\n"
   "\n"
   "@monkeys\n"
   "Feature: Monkeys behave as expected\n"
   "  Experimenters want to ensure that their monkey simulations are behaving\n"
   "  correctly.\n"
   "\n"
   "\n"
   "  # This is a comment about this scenario ...\n"
   "  @fruit-reactions\n"
   "  Scenario Outline: Monkeys behave as expected when offered various foods.\n"
   "    Given a monkey\n"
   "    When I give him a <fruit>\n"
   "    Then he is <response>\n"
   "    But he doesn't eat it\n"
   "    And he looks at me <manner_of_looking>\n"
   "   \n"
   "   \n"
   "  Examples: monkey characteristics:\n"
   "  | fruit  | response  | manner_of_looking  |\n"
   "  | banana | happy     | quizically         |\n"
   "  | pear   | sad       | loathingly         |\n"
   "   \n"
   "  # This is a comment about this scenario outline...\n"
   "\n"
   "  @orangutan\n"
   "  Scenario: Monkeys adore orangutans.\n"
   "    Given a monkey\n"
   "    When I present him with an orangutan\n"
   "    Then he is happy\n"
   "\n"
   "\n"))

(defn update-step-rets
  "Convenience testing function that appends val to the :step-rets key of the
  map context, while ensuring that the val of :step-rets is a vec."
  [context val]
  (update-in
   context
   [:step-rets]
   (fn [step-rets]
     (if (seq step-rets)
       (conj step-rets val)
       [val]))))

;; A fake registry of step functions to test our Monkey Feature
(def fake-registry
  {:given {"a monkey" (fn [context] (update-step-rets context :a-monkey))}
   :when {"I give him a banana"
          (fn [context] (update-step-rets context :give-banana))
          "I give him a pear"
          (fn [context] (update-step-rets context :give-pear))
          "I present him with an orangutan"
          (fn [context] (update-step-rets context :present-with-orang))}
   :then {"he doesn't eat it" (fn [context] (update-step-rets context :not-eat))
          "he is happy" (fn [context] (update-step-rets context :is-happy))
          "he is sad" (fn [context] (update-step-rets context :is-sad))
          "he looks at me loathingly"
          (fn [context] (update-step-rets context :looks-loathingly))
          "he looks at me quizically"
          (fn [context] (update-step-rets context :looks-quizically))}})

;; This fake registry has a step function that takes parameters
(def fake-registry-params
  (assoc-in
   fake-registry
   [:when "I give him a {fruit}"]
   (fn [context fruit]
     (update-step-rets context (keyword (format "give-with-var-%s" fruit))))))

;; This fake registry has a step function will raise an exception
(def fake-registry-error
  (assoc-in
   fake-registry
   [:then "he is happy"]
   (fn [context] (update-step-rets context (/ 1 0)))))

(defn count-scenarios
  "Return the number of scenarios in seq features."
  [[features err]]
  (if err
    0
    (reduce +
            (map (fn [feat] (-> feat :scenarios count)) features))))

;; Fake run outcome: a vec of scenario executions
(def fake-run-outcome-1
  [{:steps  ;; pass, pass, error, untested, untested
    [{:type :given
      :text "a monkey"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.389-00:00"
       :end-time #inst "2019-07-28T15:42:19.389-00:00"
       :ctx-after-exec {:step-rets [:a-monkey]}
       :err nil}}
     {:type :when
      :text "I give him a banana"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.390-00:00"
       :end-time #inst "2019-07-28T15:42:19.390-00:00"
       :ctx-after-exec {:step-rets [:a-monkey :give-with-var-banana]}
       :err nil}}
     {:type :then
      :text "he is happy"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.390-00:00"
       :end-time #inst "2019-07-28T15:42:19.391-00:00"
       :ctx-after-exec nil
       :err
       {:type :error
        :message "Divide by zero"
        :stack-trace
        (list
         "clojure.lang.Numbers.divide(Numbers.java:163)"
         "clojure.lang.Numbers.divide(Numbers.java:3833)"
         "tegere.runner_fiddle$eval17086$fn__17093.invoke(form-init5595963778114898981.clj:96)"
         "clojure.core$partial$fn__5561.invoke(core.clj:2615)"
         "clojure.lang.AFn.applyToHelper(AFn.java:152)"
         "clojure.lang.RestFn.applyTo(RestFn.java:132)"
         "clojure.core$apply.invokeStatic(core.clj:657)"
         "clojure.core$apply.invoke(core.clj:652)"
         "tegere.runner$get_step_fn$fn__16239$fn__16243.invoke(runner.clj:107)"
         "tegere.runner$call_step_fn.invokeStatic(runner.clj:193)"
         "tegere.runner$call_step_fn.invoke(runner.clj:187)"
         "tegere.runner$execute_step.invokeStatic(runner.clj:208)"
         "tegere.runner$execute_step.invoke(runner.clj:199)"
         "tegere.runner$execute_steps.invokeStatic(runner.clj:224)"
         "tegere.runner$execute_steps.invoke(runner.clj:220)"
         "tegere.runner$execute_steps.invokeStatic(runner.clj:227)"
         "tegere.runner$execute_steps.invoke(runner.clj:220)"
         "tegere.runner$execute_steps.invokeStatic(runner.clj:227)"
         "tegere.runner$execute_steps.invoke(runner.clj:220)"
         "tegere.runner$execute_steps_map.invokeStatic(runner.clj:235)"
         "tegere.runner$execute_steps_map.invoke(runner.clj:230)"
         "tegere.runner$execute_steps_map_seq.invokeStatic(runner.clj:268)"
         "tegere.runner$execute_steps_map_seq.invoke(runner.clj:261)"
         "clojure.core$partial$fn__5565.invoke(core.clj:2630)"
         "tegere.runner$execute.invokeStatic(runner.clj:288)"
         "tegere.runner$execute.invoke(runner.clj:277)"
         "clojure.core$partial$fn__5563.invoke(core.clj:2623)"
         "tegere.utils$bind.invokeStatic(utils.clj:9)"
         "tegere.utils$bind.invoke(utils.clj:4)"
         "tegere.runner$run.invokeStatic(runner.clj:435)"
         "tegere.runner$run.doInvoke(runner.clj:430)"
         "clojure.lang.RestFn.invoke(RestFn.java:445)"
         "tegere.runner_fiddle$eval17086.invokeStatic(form-init5595963778114898981.clj:102)"
         "tegere.runner_fiddle$eval17086.invoke(form-init5595963778114898981.clj:86)"
         "clojure.lang.Compiler.eval(Compiler.java:7062)"
         "clojure.lang.Compiler.eval(Compiler.java:7025)"
         "clojure.core$eval.invokeStatic(core.clj:3206)"
         "clojure.core$eval.invoke(core.clj:3202)"
         "clojure.main$repl$read_eval_print__8572$fn__8575.invoke(main.clj:243)"
         "clojure.main$repl$read_eval_print__8572.invoke(main.clj:243)"
         "clojure.main$repl$fn__8581.invoke(main.clj:261)"
         "clojure.main$repl.invokeStatic(main.clj:261)"
         "clojure.main$repl.doInvoke(main.clj:177)"
         "clojure.lang.RestFn.applyTo(RestFn.java:137)"
         "clojure.core$apply.invokeStatic(core.clj:657)"
         "clojure.core$apply.invoke(core.clj:652)"
         "refactor_nrepl.ns.slam.hound.regrow$wrap_clojure_repl$fn__12537.doInvoke(regrow.clj:18)"
         "clojure.lang.RestFn.invoke(RestFn.java:1523)"
         "nrepl.middleware.interruptible_eval$evaluate$fn__3175.invoke(interruptible_eval.clj:83)"
         "clojure.lang.AFn.applyToHelper(AFn.java:152)"
         "clojure.lang.AFn.applyTo(AFn.java:144)"
         "clojure.core$apply.invokeStatic(core.clj:657)"
         "clojure.core$with_bindings_STAR_.invokeStatic(core.clj:1965)"
         "clojure.core$with_bindings_STAR_.doInvoke(core.clj:1965)"
         "clojure.lang.RestFn.invoke(RestFn.java:425)"
         "nrepl.middleware.interruptible_eval$evaluate.invokeStatic(interruptible_eval.clj:81)"
         "nrepl.middleware.interruptible_eval$evaluate.invoke(interruptible_eval.clj:50)"
         "nrepl.middleware.interruptible_eval$interruptible_eval$fn__3218$fn__3221.invoke(interruptible_eval.clj:221)"
         "nrepl.middleware.interruptible_eval$run_next$fn__3213.invoke(interruptible_eval.clj:189)"
         "clojure.lang.AFn.run(AFn.java:22)"
         "java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)"
         "java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)"
         "java.lang.Thread.run(Thread.java:745)")}}}
     {:type :then
      :text "he doesn't eat it"
      :original-type :but
      :fn nil
      :execution nil}
     {:type :then
      :text "he looks at me quizically"
      :original-type :and
      :fn nil
      :execution nil}]
    :feature
    {:name "Monkeys behave as expected"
     :description
     "Experimenters want to ensure that their monkey simulations are behaving correctly."
     :tags (list "monkeys")}
    :scenario
    {:description "Monkeys behave as expected when offered various foods."
     :tags (list "fruit-reactions")}}
   {:steps  ;; pass, pass, pass, pass, pass
    [{:type :given
      :text "a monkey"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.392-00:00"
       :end-time #inst "2019-07-28T15:42:19.392-00:00"
       :ctx-after-exec {:step-rets [:a-monkey]}
       :err nil}}
     {:type :when
      :text "I give him a pear"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.392-00:00"
       :end-time #inst "2019-07-28T15:42:19.392-00:00"
       :ctx-after-exec {:step-rets [:a-monkey :give-with-var-pear]}
       :err nil}}
     {:type :then
      :text "he is sad"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.392-00:00"
       :end-time #inst "2019-07-28T15:42:19.392-00:00"
       :ctx-after-exec {:step-rets [:a-monkey :give-with-var-pear :is-sad]}
       :err nil}}
     {:type :then
      :text "he doesn't eat it"
      :original-type :but
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.392-00:00"
       :end-time #inst "2019-07-28T15:42:19.392-00:00"
       :ctx-after-exec
       {:step-rets [:a-monkey :give-with-var-pear :is-sad :not-eat]}
       :err nil}}
     {:type :then
      :text "he looks at me loathingly"
      :original-type :and
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.393-00:00"
       :end-time #inst "2019-07-28T15:42:19.393-00:00"
       :ctx-after-exec
       {:step-rets
        [:a-monkey :give-with-var-pear :is-sad :not-eat :looks-loathingly]}
       :err nil}}]
    :feature
    {:name "Monkeys behave as expected"
     :description
     "Experimenters want to ensure that their monkey simulations are behaving correctly."
     :tags (list "monkeys")}
    :scenario
    {:description "Monkeys behave as expected when offered various foods."
     :tags (list "fruit-reactions")}}
   {:steps  ;; pass, pass, error, untested, untested
    [{:type :given
      :text "a monkey"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.393-00:00"
       :end-time #inst "2019-07-28T15:42:19.393-00:00"
       :ctx-after-exec {:step-rets [:a-monkey]}
       :err nil}}
     {:type :when
      :text "I give him a banana"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.394-00:00"
       :end-time #inst "2019-07-28T15:42:19.394-00:00"
       :ctx-after-exec {:step-rets [:a-monkey :give-with-var-banana]}
       :err nil}}
     {:type :then
      :text "he is happy"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.394-00:00"
       :end-time #inst "2019-07-28T15:42:19.394-00:00"
       :ctx-after-exec nil
       :err
       {:type :error
        :message "Divide by zero"
        :stack-trace
        (list
         "clojure.lang.Numbers.divide(Numbers.java:163)"
         "clojure.lang.Numbers.divide(Numbers.java:3833)"
         "tegere.runner_fiddle$eval17086$fn__17093.invoke(form-init5595963778114898981.clj:96)"
         "clojure.core$partial$fn__5561.invoke(core.clj:2615)"
         "clojure.lang.AFn.applyToHelper(AFn.java:152)"
         "clojure.lang.RestFn.applyTo(RestFn.java:132)"
         "clojure.core$apply.invokeStatic(core.clj:657)"
         "clojure.core$apply.invoke(core.clj:652)"
         "tegere.runner$get_step_fn$fn__16239$fn__16243.invoke(runner.clj:107)"
         "tegere.runner$call_step_fn.invokeStatic(runner.clj:193)"
         "tegere.runner$call_step_fn.invoke(runner.clj:187)"
         "tegere.runner$execute_step.invokeStatic(runner.clj:208)"
         "tegere.runner$execute_step.invoke(runner.clj:199)"
         "tegere.runner$execute_steps.invokeStatic(runner.clj:224)"
         "tegere.runner$execute_steps.invoke(runner.clj:220)"
         "tegere.runner$execute_steps.invokeStatic(runner.clj:227)"
         "tegere.runner$execute_steps.invoke(runner.clj:220)"
         "tegere.runner$execute_steps.invokeStatic(runner.clj:227)"
         "tegere.runner$execute_steps.invoke(runner.clj:220)"
         "tegere.runner$execute_steps_map.invokeStatic(runner.clj:235)"
         "tegere.runner$execute_steps_map.invoke(runner.clj:230)"
         "tegere.runner$execute_steps_map_seq.invokeStatic(runner.clj:268)"
         "tegere.runner$execute_steps_map_seq.invoke(runner.clj:261)"
         "tegere.runner$execute_steps_map_seq.invokeStatic(runner.clj:273)"
         "tegere.runner$execute_steps_map_seq.invoke(runner.clj:261)"
         "tegere.runner$execute_steps_map_seq.invokeStatic(runner.clj:273)"
         "tegere.runner$execute_steps_map_seq.invoke(runner.clj:261)"
         "clojure.core$partial$fn__5565.invoke(core.clj:2630)"
         "tegere.runner$execute.invokeStatic(runner.clj:288)"
         "tegere.runner$execute.invoke(runner.clj:277)"
         "clojure.core$partial$fn__5563.invoke(core.clj:2623)"
         "tegere.utils$bind.invokeStatic(utils.clj:9)"
         "tegere.utils$bind.invoke(utils.clj:4)"
         "tegere.runner$run.invokeStatic(runner.clj:435)"
         "tegere.runner$run.doInvoke(runner.clj:430)"
         "clojure.lang.RestFn.invoke(RestFn.java:445)"
         "tegere.runner_fiddle$eval17086.invokeStatic(form-init5595963778114898981.clj:102)"
         "tegere.runner_fiddle$eval17086.invoke(form-init5595963778114898981.clj:86)"
         "clojure.lang.Compiler.eval(Compiler.java:7062)"
         "clojure.lang.Compiler.eval(Compiler.java:7025)"
         "clojure.core$eval.invokeStatic(core.clj:3206)"
         "clojure.core$eval.invoke(core.clj:3202)"
         "clojure.main$repl$read_eval_print__8572$fn__8575.invoke(main.clj:243)"
         "clojure.main$repl$read_eval_print__8572.invoke(main.clj:243)"
         "clojure.main$repl$fn__8581.invoke(main.clj:261)"
         "clojure.main$repl.invokeStatic(main.clj:261)"
         "clojure.main$repl.doInvoke(main.clj:177)"
         "clojure.lang.RestFn.applyTo(RestFn.java:137)"
         "clojure.core$apply.invokeStatic(core.clj:657)"
         "clojure.core$apply.invoke(core.clj:652)"
         "refactor_nrepl.ns.slam.hound.regrow$wrap_clojure_repl$fn__12537.doInvoke(regrow.clj:18)"
         "clojure.lang.RestFn.invoke(RestFn.java:1523)"
         "nrepl.middleware.interruptible_eval$evaluate$fn__3175.invoke(interruptible_eval.clj:83)"
         "clojure.lang.AFn.applyToHelper(AFn.java:152)"
         "clojure.lang.AFn.applyTo(AFn.java:144)"
         "clojure.core$apply.invokeStatic(core.clj:657)"
         "clojure.core$with_bindings_STAR_.invokeStatic(core.clj:1965)"
         "clojure.core$with_bindings_STAR_.doInvoke(core.clj:1965)"
         "clojure.lang.RestFn.invoke(RestFn.java:425)"
         "nrepl.middleware.interruptible_eval$evaluate.invokeStatic(interruptible_eval.clj:81)"
         "nrepl.middleware.interruptible_eval$evaluate.invoke(interruptible_eval.clj:50)"
         "nrepl.middleware.interruptible_eval$interruptible_eval$fn__3218$fn__3221.invoke(interruptible_eval.clj:221)"
         "nrepl.middleware.interruptible_eval$run_next$fn__3213.invoke(interruptible_eval.clj:189)"
         "clojure.lang.AFn.run(AFn.java:22)"
         "java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)"
         "java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)"
         "java.lang.Thread.run(Thread.java:745)")}}}
     {:type :then
      :text "he doesn't eat it"
      :original-type :but
      :fn nil
      :execution nil}
     {:type :then
      :text "he looks at me quizically"
      :original-type :and
      :fn nil
      :execution nil}]
    :feature
    {:name "Monkeys behave as expected"
     :description
     "Experimenters want to ensure that their monkey simulations are behaving correctly."
     :tags (list "monkeys")}
    :scenario
    {:description "Monkeys behave as expected when offered various foods."
     :tags (list "fruit-reactions")}}
   {:steps  ;; pass, pass, pass, pass, pass
    [{:type :given
      :text "a monkey"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.394-00:00"
       :end-time #inst "2019-07-28T15:42:19.394-00:00"
       :ctx-after-exec {:step-rets [:a-monkey]}
       :err nil}}
     {:type :when
      :text "I give him a pear"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.394-00:00"
       :end-time #inst "2019-07-28T15:42:19.394-00:00"
       :ctx-after-exec {:step-rets [:a-monkey :give-with-var-pear]}
       :err nil}}
     {:type :then
      :text "he is sad"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.395-00:00"
       :end-time #inst "2019-07-28T15:42:19.395-00:00"
       :ctx-after-exec {:step-rets [:a-monkey :give-with-var-pear :is-sad]}
       :err nil}}
     {:type :then
      :text "he doesn't eat it"
      :original-type :but
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.395-00:00"
       :end-time #inst "2019-07-28T15:42:19.395-00:00"
       :ctx-after-exec
       {:step-rets [:a-monkey :give-with-var-pear :is-sad :not-eat]}
       :err nil}}
     {:type :then
      :text "he looks at me loathingly"
      :original-type :and
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.395-00:00"
       :end-time #inst "2019-07-28T15:42:19.395-00:00"
       :ctx-after-exec
       {:step-rets
        [:a-monkey :give-with-var-pear :is-sad :not-eat :looks-loathingly]}
       :err nil}}]
    :feature
    {:name "Monkeys behave as expected"
     :description
     "Experimenters want to ensure that their monkey simulations are behaving correctly."
     :tags (list "monkeys")}
    :scenario
    {:description "Monkeys behave as expected when offered various foods."
     :tags (list "fruit-reactions")}}])

;; Fake run outcome 2: a vec of scenario executions
(def fake-run-outcome-2
  [{:steps
    [{:type :when
      :text
      "a well-formed request is made to update the monkey-integrated liquidity for space 3170 of pork 651 owned by company 13"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T16:09:24.804-00:00"
       :end-time #inst "2019-07-28T16:09:26.200-00:00"
       :ctx-after-exec
       {:update-monkey-liquidity-resp
        [{:status "success" :updated-at "2019-07-28T12:09:25.943674"} nil]}
       :err nil}}
     {:type :then
      :text "a successful response is received"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T16:09:26.201-00:00"
       :end-time #inst "2019-07-28T16:09:26.201-00:00"
       :ctx-after-exec {:step-return-value nil}
       :err nil}}]
    :feature
    {:name "the porkcase monkey integration liquidity endpoint works"
     :description
     "porkcase wants to ensure that requests to the monkey integration liquidity endpoint are handled correctly."
     :tags (list "monkey" "liquidity")}
    :scenario
    {:description
     "Well-formed update requests to the monkey liquidity endpoint are handled correctly."
     :tags (list "update")}}])

;; Like fake-run-outcome-3 but split across two scenarios in one feature.
(def fake-run-outcome-3
  [{:steps  ;; pass, pass, error, untested, untested
    [{:type :given
      :text "a monkey"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.389-00:00"
       :end-time #inst "2019-07-28T15:42:19.389-00:00"
       :ctx-after-exec {:step-rets [:a-monkey]}
       :err nil}}
     {:type :when
      :text "I give him a banana"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.390-00:00"
       :end-time #inst "2019-07-28T15:42:19.390-00:00"
       :ctx-after-exec {:step-rets [:a-monkey :give-with-var-banana]}
       :err nil}}
     {:type :then
      :text "he is happy"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.390-00:00"
       :end-time #inst "2019-07-28T15:42:19.391-00:00"
       :ctx-after-exec nil
       :err
       {:type :error
        :message "Divide by zero"
        :stack-trace
        (list
         "clojure.lang.Numbers.divide(Numbers.java:163)"
         "java.lang.Thread.run(Thread.java:745)")}}}
     {:type :then
      :text "he doesn't eat it"
      :original-type :but
      :fn nil
      :execution nil}
     {:type :then
      :text "he looks at me quizically"
      :original-type :and
      :fn nil
      :execution nil}]
    :feature
    {:name "Monkeys behave as expected"
     :description
     "Experimenters want to ensure that their monkey simulations are behaving correctly."
     :tags (list "monkeys")}
    :scenario
    {:description "A"
     :tags (list "a")}}
   {:steps  ;; pass, pass, pass, pass, pass
    [{:type :given
      :text "a monkey"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.392-00:00"
       :end-time #inst "2019-07-28T15:42:19.392-00:00"
       :ctx-after-exec {:step-rets [:a-monkey]}
       :err nil}}
     {:type :when
      :text "I give him a pear"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.392-00:00"
       :end-time #inst "2019-07-28T15:42:19.392-00:00"
       :ctx-after-exec {:step-rets [:a-monkey :give-with-var-pear]}
       :err nil}}
     {:type :then
      :text "he is sad"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.392-00:00"
       :end-time #inst "2019-07-28T15:42:19.392-00:00"
       :ctx-after-exec {:step-rets [:a-monkey :give-with-var-pear :is-sad]}
       :err nil}}
     {:type :then
      :text "he doesn't eat it"
      :original-type :but
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.392-00:00"
       :end-time #inst "2019-07-28T15:42:19.392-00:00"
       :ctx-after-exec
       {:step-rets [:a-monkey :give-with-var-pear :is-sad :not-eat]}
       :err nil}}
     {:type :then
      :text "he looks at me loathingly"
      :original-type :and
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.393-00:00"
       :end-time #inst "2019-07-28T15:42:19.393-00:00"
       :ctx-after-exec
       {:step-rets
        [:a-monkey :give-with-var-pear :is-sad :not-eat :looks-loathingly]}
       :err nil}}]
    :feature
    {:name "Monkeys behave as expected"
     :description
     "Experimenters want to ensure that their monkey simulations are behaving correctly."
     :tags (list "monkeys")}
    :scenario
    {:description "A"
     :tags (list "a")}}
   {:steps  ;; pass, pass, error, untested, untested
    [{:type :given
      :text "a monkey"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.393-00:00"
       :end-time #inst "2019-07-28T15:42:19.393-00:00"
       :ctx-after-exec {:step-rets [:a-monkey]}
       :err nil}}
     {:type :when
      :text "I give him a banana"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.394-00:00"
       :end-time #inst "2019-07-28T15:42:19.394-00:00"
       :ctx-after-exec {:step-rets [:a-monkey :give-with-var-banana]}
       :err nil}}
     {:type :then
      :text "he is happy"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.394-00:00"
       :end-time #inst "2019-07-28T15:42:19.394-00:00"
       :ctx-after-exec nil
       :err
       {:type :error
        :message "Divide by zero"
        :stack-trace
        (list
         "clojure.lang.Numbers.divide(Numbers.java:163)"
         "java.lang.Thread.run(Thread.java:745)")}}}
     {:type :then
      :text "he doesn't eat it"
      :original-type :but
      :fn nil
      :execution nil}
     {:type :then
      :text "he looks at me quizically"
      :original-type :and
      :fn nil
      :execution nil}]
    :feature
    {:name "Monkeys behave as expected"
     :description
     "Experimenters want to ensure that their monkey simulations are behaving correctly."
     :tags (list "monkeys")}
    :scenario
    {:description "B"
     :tags (list "b")}}
   {:steps  ;; pass, pass, pass, pass, pass
    [{:type :given
      :text "a monkey"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.394-00:00"
       :end-time #inst "2019-07-28T15:42:19.394-00:00"
       :ctx-after-exec {:step-rets [:a-monkey]}
       :err nil}}
     {:type :when
      :text "I give him a pear"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.394-00:00"
       :end-time #inst "2019-07-28T15:42:19.394-00:00"
       :ctx-after-exec {:step-rets [:a-monkey :give-with-var-pear]}
       :err nil}}
     {:type :then
      :text "he is sad"
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.395-00:00"
       :end-time #inst "2019-07-28T15:42:19.395-00:00"
       :ctx-after-exec {:step-rets [:a-monkey :give-with-var-pear :is-sad]}
       :err nil}}
     {:type :then
      :text "he doesn't eat it"
      :original-type :but
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.395-00:00"
       :end-time #inst "2019-07-28T15:42:19.395-00:00"
       :ctx-after-exec
       {:step-rets [:a-monkey :give-with-var-pear :is-sad :not-eat]}
       :err nil}}
     {:type :then
      :text "he looks at me loathingly"
      :original-type :and
      :fn nil
      :execution
      {:start-time #inst "2019-07-28T15:42:19.395-00:00"
       :end-time #inst "2019-07-28T15:42:19.395-00:00"
       :ctx-after-exec
       {:step-rets
        [:a-monkey :give-with-var-pear :is-sad :not-eat :looks-loathingly]}
       :err nil}}]
    :feature
    {:name "Monkeys behave as expected"
     :description
     "Experimenters want to ensure that their monkey simulations are behaving correctly."
     :tags (list "monkeys")}
    :scenario
    {:description "B"
     :tags (list "b")}}])

(t/deftest all-tags-filtering-test
  (t/testing "All/and tag filters work"
    (let [features [(parse monkey-feature)]
          scenario-count (count-scenarios [features nil])
          get-match-count (fn [expr]
                            (count-scenarios
                             (sut/get-features-to-run
                              expr features)))]
      (do
        (t/is (= 3 scenario-count))
        (t/is (= 3 (get-match-count
                    {:and-tags #{"monkeys"}})))
        (t/is (= 1 (get-match-count
                    {:and-tags #{"monkeys" "orangutan"}})))
        (t/is (= 1 (get-match-count
                    {:and-tags #{"orangutan"}})))
        (t/is (= 2 (get-match-count
                    {:and-tags #{"monkeys" "fruit-reactions"}})))
        (t/is (= 2 (get-match-count
                    {:and-tags #{"fruit-reactions"}})))
        (t/is (= 0 (get-match-count
                    {:and-tags #{"orangutan" "fruit-reactions"}})))
        (t/is (= 0 (get-match-count
                    {:and-tags #{"fake-tag"}})))))))

(t/deftest any-tags-filtering-test
  (t/testing "Any/or tag filters work"
    (let [features [(parse monkey-feature)]
          scenario-count (count-scenarios [features nil])
          get-match-count (fn [expr]
                            (count-scenarios
                             (sut/get-features-to-run
                              expr features)))]
      (do
        (t/is (= 3 (get-match-count
                    {:or-tags #{"monkeys"}})))
        (t/is (= 3 (get-match-count
                    {:or-tags #{"monkeys" "orangutan"}})))
        (t/is (= 1 (get-match-count
                    {:or-tags #{"orangutan"}})))
        (t/is (= 3 (get-match-count
                    {:or-tags #{"monkeys" "fruit-reactions"}})))
        (t/is (= 2 (get-match-count
                    {:or-tags #{"fruit-reactions"}})))
        (t/is (= 3 (get-match-count
                    {:or-tags #{"orangutan" "fruit-reactions"}})))
        (t/is (= 0 (get-match-count
                    {:or-tags #{"fake-tag"}})))))))

(t/deftest all-tags-override-or-tags-test
  (t/testing "All/and tag filters override any/or tag filters"
    (let [features [(parse monkey-feature)]
          scenario-count (count-scenarios [features nil])
          get-match-count (fn [expr]
                            (count-scenarios
                             (sut/get-features-to-run
                              expr features)))]
      (do
        (t/is (= 3 (get-match-count
                    {:and-tags #{"monkeys"}
                     :or-tags #{"monkeys"}})))
        (t/is (= 1 (get-match-count
                    {:and-tags #{"monkeys" "orangutan"}
                     :or-tags #{"monkeys" "orangutan"}})))
        (t/is (= 1 (get-match-count
                    {:and-tags #{"orangutan"}
                     :or-tags #{"orangutan"}})))
        (t/is (= 2 (get-match-count
                    {:and-tags #{"monkeys" "fruit-reactions"}
                     :or-tags #{"monkeys" "fruit-reactions"}})))
        (t/is (= 2 (get-match-count
                    {:and-tags #{"fruit-reactions"}
                     :or-tags #{"fruit-reactions"}})))
        (t/is (= 0 (get-match-count
                    {:and-tags #{"orangutan" "fruit-reactions"}
                     :or-tags #{"orangutan" "fruit-reactions"}})))
        (t/is (= 0 (get-match-count
                    {:and-tags #{"fake-tag"}
                     :or-tags #{"fake-tag"}})))))))

(t/deftest can-run-simple-feature-test
  (t/testing "We can run a simple feature"
    (let [features [(parse monkey-feature)]
          execution (sut/run features fake-registry {})
          exec-steps (->> execution (map :steps) flatten)
          exec-step-count (count exec-steps)
          success-exec-steps (filter
                              (fn [s] (nil? (-> s :execution :err))) exec-steps)
          success-exec-step-count (count success-exec-steps)
          exec-step-rets
          (map (fn [s]
                 (get-in s [:execution :ctx-after-exec :step-rets])) exec-steps)]
      (do
        ;; Feature with 1 2-line Scenario Outline and 1 Scenario should result
        ;; in 3 step-maps in the execution:
        (t/is (= 3 (count execution)))
        ;; The 2 Scenario Outlines have 5 steps each and the sole Scenario has
        ;; 3 steps; expect 13 steps in the execution:
        (t/is (= 13 exec-step-count))
        ;; All 13 step executions should have been successful
        (t/is (= exec-step-count success-exec-step-count))
        ;; The contexts at the end of execution of each scenario shoulld reflect
        ;; the meaning of the scenario (because the fake registry is defined that
        ;; way.)
        (t/is (= [[:a-monkey :give-banana :is-happy :not-eat :looks-quizically]
                  [:a-monkey :give-pear :is-sad :not-eat :looks-loathingly]
                  [:a-monkey :present-with-orang :is-happy]]
                 [(nth exec-step-rets 4) (nth exec-step-rets 9)
                  (nth exec-step-rets 12)]))))))

(t/deftest step-fns-can-take-args-test
  (t/testing "step functions can take arguments"
    (do
      ;; Match with 2 args
      (t/is (= (sut/get-step-fn-args "I give {recipient} a {fruit}"
                                     "I give him a big ole banana")
               (list "him" "big ole banana")))
      ;; No match
      (t/is (= (sut/get-step-fn-args "I gave {recipient} a {fruit}"
                                     "I give him a big ole banana")
               nil))
      ;; Match with 0 args
      (t/is (= (sut/get-step-fn-args "I give him a big ole banana"
                                     "I give him a big ole banana")
               ())))))

(t/deftest step-functions-that-take-params-test
  (t/testing "We can run a simple feature against a steps registry with steps
             that take parameters"
    (let [features [(parse monkey-feature)]
          execution (sut/run features fake-registry-error {})
          exec-steps (->> execution (map :steps) flatten)
          exec-step-count (count exec-steps)
          success-exec-steps (filter
                              (fn [s] (nil? (-> s :execution :err))) exec-steps)
          success-exec-step-count (count success-exec-steps)
          exec-step-rets
          (map (fn [s]
                 (get-in s [:execution :ctx-after-exec :step-rets])) exec-steps)]
      (do
        ;; Feature with 1 2-line Scenario Outline and 1 Scenario should result
        ;; in 3 step-maps in the execution:
        (t/is (= 3 (count execution)))
        ;; The 2 Scenario Outlines have 5 steps each and the sole Scenario has
        ;; 3 steps; expect 13 steps in the execution:
        (t/is (= 13 exec-step-count))
        ;; The 2 "Then he is happy" steps fail because of the error registry.
        (t/is (= 11 success-exec-step-count))
        ;; The contexts at the end of execution of each scenario shoulld reflect
        ;; the meaning of the scenario (because the fake registry is defined that
        ;; way.)
        (t/is (= [nil [:a-monkey :give-pear :is-sad :not-eat :looks-loathingly]
                  nil]
                 [(nth exec-step-rets 4) (nth exec-step-rets 9)
                  (nth exec-step-rets 12)]))))))

(t/deftest stop-flag-works-test
  (t/testing "The :stop true flag tells the test runner to stop running
             scenarios after the first one fails."
    (let [features [(parse monkey-feature)]
          execution (sut/run features fake-registry-error {:stop true})
          exec-steps (->> execution (map :steps) flatten)
          exec-step-count (count exec-steps)
          success-exec-steps (filter
                              (fn [s] (nil? (-> s :execution :err))) exec-steps)
          success-exec-step-count (count success-exec-steps)
          exec-step-rets
          (map (fn [s]
                 (get-in s [:execution :ctx-after-exec :step-rets])) exec-steps)]
      (do
        ;; There is only 1 failure because the second is not allowed to happen.
        (t/is (= 12 success-exec-step-count))
        ;; The :step-rets key in the context of the end of each scenario should
        ;; be absent because no scenarios complete successfully.
        (t/is (= [nil nil nil]
                 [(nth exec-step-rets 4) (nth exec-step-rets 9)
                  (nth exec-step-rets 12)]))))))

(t/deftest analyze-step-execution-test
  (t/testing "That analyze-step-execution gives the correct outcome and step
             counts."
    (t/are
        [execution expected]
        (= (-> execution
               sut/analyze-step-execution
               (select-keys [:step-pass-count
                             :step-untested-count
                             :step-fail-count
                             :execution-pass-count
                             :execution-fail-count
                             :outcome]))
           expected)

      (first fake-run-outcome-1)
      {:step-pass-count 2
       :step-untested-count 2
       :step-fail-count 1
       :execution-pass-count 0
       :execution-fail-count 1
       :outcome :error}

      (second fake-run-outcome-1)
      {:step-pass-count 5
       :step-untested-count 0
       :step-fail-count 0
       :execution-pass-count 1
       :execution-fail-count 0
       :outcome :pass}

      (nth fake-run-outcome-1 2)
      {:step-pass-count 2
       :step-untested-count 2
       :step-fail-count 1
       :execution-pass-count 0
       :execution-fail-count 1
       :outcome :error}

      (nth fake-run-outcome-1 3)
      {:step-pass-count 5
       :step-untested-count 0
       :step-fail-count 0
       :execution-pass-count 1
       :execution-fail-count 0
       :outcome :pass}

      (first fake-run-outcome-2)
      {:step-pass-count 2
       :step-untested-count 0
       :step-fail-count 0
       :execution-pass-count 1
       :execution-fail-count 0
       :outcome :pass})))

(t/deftest produce-outcome-map-test
  (t/testing "That executions->outcome-map gives the correct aggregate step and
             execution pass/fail/untested counts."
    (t/are
        [run-outcome expected]
        (= (->> run-outcome
                sut/executions->outcome-map
                vals
                (map vals)
                flatten
                first)
           expected)
      fake-run-outcome-1
      {:step-pass-count 14
       :step-untested-count 4
       :step-fail-count 2
       :execution-pass-count 2
       :execution-fail-count 2}

      fake-run-outcome-2
      {:step-pass-count 2
       :step-untested-count 0
       :step-fail-count 0
       :execution-pass-count 1
       :execution-fail-count 0})))

(t/deftest get-outcome-summary-test
  (t/testing "That get-outcome-summary gives the correct outcome summary data
              structure for various test run outcomes."
    (t/are
        [run-outcome expected]
        (= expected (sut/get-outcome-summary run-outcome :as-data? true))

      ;; 2 features, each with 1 scenario; first feature fails because its
      ;; scenario fails because one of its step executions errors; second feature
      ;; passes because its sole scenario does.
      [{:steps  ;; pass, pass, error, untested, untested
        [{:execution {:err nil}}
         {:execution {:err nil}}
         {:execution {:err {:type :error}}}
         {:execution nil}
         {:execution nil}]
        :feature "f-a"
        :scenario "s-a"}
       {:steps  ;; pass, pass
        [{:execution {:err nil}}
         {:execution {:err nil}}]
        :feature "f-a"
        :scenario "s-a"}
       {:steps  ;; pass, pass
        [{:execution {:err nil}}
         {:execution {:err nil}}]
        :feature "f-b"
        :scenario "s-b"}]
      {:step-pass-count 6
       :step-untested-count 2
       :step-fail-count 1
       :scenario-pass-count 1
       :scenario-fail-count 1
       :feature-pass-count 1
       :feature-fail-count 1})))
