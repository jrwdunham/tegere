(ns tegere.runner-test
  (:require [clojure.test :as t]
            [tegere.runner :as sut]
            [tegere.parser :as parser]))

;; A feature file (string) to use for testing
(def chimpanzee-feature
  (str
   "# This is a comment about this feature\n"
   "\n"
   "@chimpanzees\n"
   "Feature: Chimpanzees behave as expected\n"
   "  Experimenters want to ensure that their chimpanzee simulations are behaving\n"
   "  correctly.\n"
   "\n"
   "\n"
   "  # This is a comment about this scenario ...\n"
   "  @fruit-reactions\n"
   "  Scenario Outline: Chimpanzees behave as expected when offered various foods.\n"
   "    Given a chimpanzee\n"
   "    When I give him a <fruit>\n"
   "    Then he is <response>\n"
   "    But he doesn't eat it\n"
   "    And he looks at me <manner_of_looking>\n"
   "   \n"
   "   \n"
   "  Examples: chimpanzee characteristics:\n"
   "  | fruit  | response  | manner_of_looking  |\n"
   "  | banana | happy     | quizzically         |\n"
   "  | pear   | sad       | loathingly         |\n"
   "   \n"
   "  # This is a comment about this scenario outline...\n"
   "\n"
   "  @orangutan\n"
   "  Scenario: Chimpanzees adore orangutans.\n"
   "    Given a chimpanzee\n"
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

;; A fake registry of step functions to test our Chimpanzee Feature
(def fake-registry
  {:given {"a chimpanzee" (fn [context] (update-step-rets context :a-chimpanzee))}
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
          "he looks at me quizzically"
          (fn [context] (update-step-rets context :looks-quizzically))}})

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
            (map (fn [feat] (-> feat ::parser/scenarios count)) features))))

;; Fake run outcome: a vec of scenario executions
(def fake-run-outcome-1
  [{::parser/steps  ;; pass, pass, error, untested, untested
    [{::parser/type :given
      ::parser/text "a chimpanzee"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.389-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.389-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee]}
       ::sut/err nil}}
     {::parser/type :when
      ::parser/text "I give him a banana"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.390-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.390-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee :give-with-var-banana]}
       ::sut/err nil}}
     {::parser/type :then
      ::parser/text "he is happy"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.390-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.391-00:00"
       ::sut/ctx-after-exec nil
       ::sut/err
       {::sut/type :error
        ::sut/message "Divide by zero"
        ::sut/stack-trace
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
     {::parser/type :then
      ::parser/text "he doesn't eat it"
      ::parser/original-type :but
      ::sut/fn nil
      ::sut/execution nil}
     {::parser/type :then
      ::parser/text "he looks at me quizzically"
      ::parser/original-type :and
      ::sut/fn nil
      ::sut/execution nil}]
    ::parser/feature
    {::parser/name "Chimpanzees behave as expected"
     ::parser/description
     "Experimenters want to ensure that their chimpanzee simulations are behaving correctly."
     ::parser/tags (list "chimpanzees")}
    ::parser/scenario
    {::parser/description "Chimpanzees behave as expected when offered various foods."
     ::parser/tags (list "fruit-reactions")}}
   {::parser/steps  ;; pass, pass, pass, pass, pass
    [{::parser/type :given
      ::parser/text "a chimpanzee"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.392-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.392-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee]}
       ::sut/err nil}}
     {::parser/type :when
      ::parser/text "I give him a pear"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.392-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.392-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee :give-with-var-pear]}
       ::sut/err nil}}
     {::parser/type :then
      ::parser/text "he is sad"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.392-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.392-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee :give-with-var-pear :is-sad]}
       ::sut/err nil}}
     {::parser/type :then
      ::parser/text "he doesn't eat it"
      ::parser/original-type :but
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.392-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.392-00:00"
       ::sut/ctx-after-exec
       {:step-rets [:a-chimpanzee :give-with-var-pear :is-sad :not-eat]}
       ::sut/err nil}}
     {::parser/type :then
      ::parser/text "he looks at me loathingly"
      ::parser/original-type :and
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.393-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.393-00:00"
       ::sut/ctx-after-exec
       {:step-rets
        [:a-chimpanzee :give-with-var-pear :is-sad :not-eat :looks-loathingly]}
       ::sut/err nil}}]
    ::parser/feature
    {::parser/name "Chimpanzees behave as expected"
     ::parser/description
     "Experimenters want to ensure that their chimpanzee simulations are behaving correctly."
     ::parser/tags (list "chimpanzees")}
    ::parser/scenario
    {::parser/description "Chimpanzees behave as expected when offered various foods."
     ::parser/tags (list "fruit-reactions")}}
   {::parser/steps  ;; pass, pass, error, untested, untested
    [{::parser/type :given
      ::parser/text "a chimpanzee"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.393-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.393-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee]}
       ::sut/err nil}}
     {::parser/type :when
      ::parser/text "I give him a banana"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.394-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.394-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee :give-with-var-banana]}
       ::sut/err nil}}
     {::parser/type :then
      ::parser/text "he is happy"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.394-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.394-00:00"
       ::sut/ctx-after-exec nil
       ::sut/err
       {::sut/type :error
        ::sut/message "Divide by zero"
        ::sut/stack-trace
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
     {::parser/type :then
      ::parser/text "he doesn't eat it"
      ::parser/original-type :but
      ::sut/fn nil
      ::sut/execution nil}
     {::parser/type :then
      ::parser/text "he looks at me quizzically"
      ::parser/original-type :and
      ::sut/fn nil
      ::sut/execution nil}]
    ::parser/feature
    {::parser/name "Chimpanzees behave as expected"
     ::parser/description
     "Experimenters want to ensure that their chimpanzee simulations are behaving correctly."
     ::parser/tags (list "chimpanzees")}
    ::parser/scenario
    {::parser/description "Chimpanzees behave as expected when offered various foods."
     ::parser/tags (list "fruit-reactions")}}
   {::parser/steps  ;; pass, pass, pass, pass, pass
    [{::parser/type :given
      ::parser/text "a chimpanzee"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.394-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.394-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee]}
       ::sut/err nil}}
     {::parser/type :when
      ::parser/text "I give him a pear"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.394-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.394-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee :give-with-var-pear]}
       ::sut/err nil}}
     {::parser/type :then
      ::parser/text "he is sad"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.395-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.395-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee :give-with-var-pear :is-sad]}
       ::sut/err nil}}
     {::parser/type :then
      ::parser/text "he doesn't eat it"
      ::parser/original-type :but
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.395-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.395-00:00"
       ::sut/ctx-after-exec
       {:step-rets [:a-chimpanzee :give-with-var-pear :is-sad :not-eat]}
       ::sut/err nil}}
     {::parser/type :then
      ::parser/text "he looks at me loathingly"
      ::parser/original-type :and
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.395-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.395-00:00"
       ::sut/ctx-after-exec
       {:step-rets
        [:a-chimpanzee :give-with-var-pear :is-sad :not-eat :looks-loathingly]}
       ::sut/err nil}}]
    ::parser/feature
    {::parser/name "Chimpanzees behave as expected"
     ::parser/description
     "Experimenters want to ensure that their chimpanzee simulations are behaving correctly."
     ::parser/tags (list "chimpanzees")}
    ::parser/scenario
    {::parser/description "Chimpanzees behave as expected when offered various foods."
     ::parser/tags (list "fruit-reactions")}}])

;; Fake run outcome 2: a vec of scenario executions
(def fake-run-outcome-2
  [{::parser/steps
    [{::parser/type :when
      ::parser/text
      "a well-formed request is made to update the chimpanzee-integrated liquidity for space 3170 of pork 651 owned by company 13"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T16:09:24.804-00:00"
       ::sut/end-time #inst "2019-07-28T16:09:26.200-00:00"
       ::sut/ctx-after-exec
       {:update-chimpanzee-liquidity-resp
        [{:status "success" :updated-at "2019-07-28T12:09:25.943674"} nil]}
       ::sut/err nil}}
     {::parser/type :then
      ::parser/text "a successful response is received"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T16:09:26.201-00:00"
       ::sut/end-time #inst "2019-07-28T16:09:26.201-00:00"
       ::sut/ctx-after-exec {:step-return-value nil}
       ::sut/err nil}}]
    ::parser/feature
    {::parser/name "the porkcase chimpanzee integration liquidity endpoint works"
     ::parser/description
     "porkcase wants to ensure that requests to the chimpanzee integration liquidity endpoint are handled correctly."
     ::parser/tags (list "chimpanzee" "liquidity")}
    ::parser/scenario
    {::parser/description
     "Well-formed update requests to the chimpanzee liquidity endpoint are handled correctly."
     ::parser/tags (list "update")}}])

;; Like fake-run-outcome-3 but split across two scenarios in one feature.
(def fake-run-outcome-3
  [{::parser/steps  ;; pass, pass, error, untested, untested
    [{::parser/type :given
      ::parser/text "a chimpanzee"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.389-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.389-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee]}
       ::sut/err nil}}
     {::parser/type :when
      ::parser/text "I give him a banana"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.390-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.390-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee :give-with-var-banana]}
       ::sut/err nil}}
     {::parser/type :then
      ::parser/text "he is happy"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.390-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.391-00:00"
       ::sut/ctx-after-exec nil
       ::sut/err
       {::sut/type :error
        ::sut/message "Divide by zero"
        ::sut/stack-trace
        (list
         "clojure.lang.Numbers.divide(Numbers.java:163)"
         "java.lang.Thread.run(Thread.java:745)")}}}
     {::parser/type :then
      ::parser/text "he doesn't eat it"
      ::parser/original-type :but
      ::sut/fn nil
      ::sut/execution nil}
     {::parser/type :then
      ::parser/text "he looks at me quizzically"
      ::parser/original-type :and
      ::sut/fn nil
      ::sut/execution nil}]
    ::parser/feature
    {::parser/name "Chimpanzees behave as expected"
     ::parser/description
     "Experimenters want to ensure that their chimpanzee simulations are behaving correctly."
     ::parser/tags (list "chimpanzees")}
    ::parser/scenario
    {::parser/description "A"
     ::parser/tags (list "a")}}
   {::parser/steps  ;; pass, pass, pass, pass, pass
    [{::parser/type :given
      ::parser/text "a chimpanzee"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.392-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.392-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee]}
       ::sut/err nil}}
     {::parser/type :when
      ::parser/text "I give him a pear"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.392-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.392-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee :give-with-var-pear]}
       ::sut/err nil}}
     {::parser/type :then
      ::parser/text "he is sad"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.392-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.392-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee :give-with-var-pear :is-sad]}
       ::sut/err nil}}
     {::parser/type :then
      ::parser/text "he doesn't eat it"
      ::parser/original-type :but
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.392-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.392-00:00"
       ::sut/ctx-after-exec
       {:step-rets [:a-chimpanzee :give-with-var-pear :is-sad :not-eat]}
       ::sut/err nil}}
     {::parser/type :then
      ::parser/text "he looks at me loathingly"
      ::parser/original-type :and
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.393-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.393-00:00"
       ::sut/ctx-after-exec
       {:step-rets
        [:a-chimpanzee :give-with-var-pear :is-sad :not-eat :looks-loathingly]}
       ::sut/err nil}}]
    ::parser/feature
    {::parser/name "Chimpanzees behave as expected"
     ::parser/description
     "Experimenters want to ensure that their chimpanzee simulations are behaving correctly."
     ::parser/tags (list "chimpanzees")}
    ::parser/scenario
    {::parser/description "A"
     ::parser/tags (list "a")}}
   {::parser/steps  ;; pass, pass, error, untested, untested
    [{::parser/type :given
      ::parser/text "a chimpanzee"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.393-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.393-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee]}
       ::sut/err nil}}
     {::parser/type :when
      ::parser/text "I give him a banana"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.394-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.394-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee :give-with-var-banana]}
       ::sut/err nil}}
     {::parser/type :then
      ::parser/text "he is happy"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.394-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.394-00:00"
       ::sut/ctx-after-exec nil
       ::sut/err
       {::sut/type :error
        ::sut/message "Divide by zero"
        ::sut/stack-trace
        (list
         "clojure.lang.Numbers.divide(Numbers.java:163)"
         "java.lang.Thread.run(Thread.java:745)")}}}
     {::parser/type :then
      ::parser/text "he doesn't eat it"
      ::parser/original-type :but
      ::sut/fn nil
      ::sut/execution nil}
     {::parser/type :then
      ::parser/text "he looks at me quizzically"
      ::parser/original-type :and
      ::sut/fn nil
      ::sut/execution nil}]
    ::parser/feature
    {::parser/name "Chimpanzees behave as expected"
     ::parser/description
     "Experimenters want to ensure that their chimpanzee simulations are behaving correctly."
     ::parser/tags (list "chimpanzees")}
    ::parser/scenario
    {::parser/description "B"
     ::parser/tags (list "b")}}
   {::parser/steps  ;; pass, pass, pass, pass, pass
    [{::parser/type :given
      ::parser/text "a chimpanzee"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.394-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.394-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee]}
       ::sut/err nil}}
     {::parser/type :when
      ::parser/text "I give him a pear"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.394-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.394-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee :give-with-var-pear]}
       ::sut/err nil}}
     {::parser/type :then
      ::parser/text "he is sad"
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.395-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.395-00:00"
       ::sut/ctx-after-exec {:step-rets [:a-chimpanzee :give-with-var-pear :is-sad]}
       ::sut/err nil}}
     {::parser/type :then
      ::parser/text "he doesn't eat it"
      ::parser/original-type :but
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.395-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.395-00:00"
       ::sut/ctx-after-exec
       {:step-rets [:a-chimpanzee :give-with-var-pear :is-sad :not-eat]}
       ::sut/err nil}}
     {::parser/type :then
      ::parser/text "he looks at me loathingly"
      ::parser/original-type :and
      ::sut/fn nil
      ::sut/execution
      {::sut/start-time #inst "2019-07-28T15:42:19.395-00:00"
       ::sut/end-time #inst "2019-07-28T15:42:19.395-00:00"
       ::sut/ctx-after-exec
       {:step-rets
        [:a-chimpanzee :give-with-var-pear :is-sad :not-eat :looks-loathingly]}
       ::sut/err nil}}]
    ::parser/feature
    {::parser/name "Chimpanzees behave as expected"
     ::parser/description
     "Experimenters want to ensure that their chimpanzee simulations are behaving correctly."
     ::parser/tags (list "chimpanzees")}
    ::parser/scenario
    {::parser/description "B"
     ::parser/tags (list "b")}}])

(defmacro ignore-out-err
  "Evaluates exprs in a context in which *out* and *err* ire bound to a fresh
  StringWriter. Effectively suppresses writes to stdout and stderr."
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s# *err* s#]
       ~@body)))

(t/deftest query-tree-based-filtering-test
  (t/testing "Filtering using a ::tegere.query/query-tree works"
    (let [features [(parser/parse chimpanzee-feature)]
          get-match-count (fn [query]
                            (count-scenarios
                             (sut/get-features-to-run query features)))
          expectations
          [[3 nil]
           [3 "@chimpanzees"]
           [1 "@chimpanzees and @orangutan"]
           [1 "@orangutan"]
           [2 "@chimpanzees and @fruit-reactions"]
           [2 "@fruit-reactions"]
           [0 "@orangutan and @fruit-reactions"]
           [0 "@fake-tag"]
           [1 "@manner_of_looking=quizzically"]
           [3 "@chimpanzees or @orangutan"]
           [3 "@fruit-reactions or @orangutan"]
           [3 "@chimpanzees or @chimpanzees"]
           [1 "((@chimpanzees and @orangutan) and (@chimpanzees or @orangutan))"]
           [1 "(@chimpanzees and @fruit=banana)"]
           [0 "(@bonobos and @fruit=pear)"]
           [3 (str "((@chimpanzees and @fruit=banana) or"
                   " (@fruit-reactions and @manner_of_looking=loathingly) or"
                   " (@chimpanzees and @orangutan))")]
           [1 (str "((@fruit=banana or @fruit=pear) and"
                   " (not @manner_of_looking=loathingly))")]]]
      (doseq [[matches query] expectations]
        (t/is (= matches
                 (get-match-count
                  (when query
                    (parser/parse-tag-expression-with-fallback
                     query)))))))))

(comment

  (let [features [(parser/parse chimpanzee-feature)]
        get-match-count (fn [query]
                          (count-scenarios
                           (sut/get-features-to-run query features)))]
    (get-match-count
     (parser/parse-tag-expression-with-fallback
        "(@chimpanzees and @fruit=pear)")))

)

(t/deftest can-run-simple-feature-test
  (t/testing "We can run a simple feature"
    (let [features [(parser/parse chimpanzee-feature)]
          execution (::sut/executables (ignore-out-err (sut/run features fake-registry {})))
          exec-steps (->> execution (map ::parser/steps) flatten)
          exec-step-count (count exec-steps)
          success-exec-steps (filter
                              (fn [s] (nil? (-> s ::sut/execution ::sut/err))) exec-steps)
          success-exec-step-count (count success-exec-steps)
          exec-step-rets
          (map (fn [s]
                 (get-in s [::sut/execution ::sut/ctx-after-exec :step-rets])) exec-steps)]
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
      (t/is (= [[:a-chimpanzee :give-banana :is-happy :not-eat :looks-quizzically]
                [:a-chimpanzee :give-pear :is-sad :not-eat :looks-loathingly]
                [:a-chimpanzee :present-with-orang :is-happy]]
               [(nth exec-step-rets 4) (nth exec-step-rets 9)
                (nth exec-step-rets 12)])))))

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
    (let [features [(parser/parse chimpanzee-feature)]
          execution (::sut/executables
                     (ignore-out-err (sut/run features fake-registry-error {})))
          exec-steps (->> execution (map ::parser/steps) flatten)
          exec-step-count (count exec-steps)
          success-exec-steps (filter
                              (fn [s] (nil? (-> s ::sut/execution ::sut/err))) exec-steps)
          success-exec-step-count (count success-exec-steps)
          exec-step-rets
          (map (fn [s]
                 (get-in s [::sut/execution ::sut/ctx-after-exec :step-rets])) exec-steps)]
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
      (t/is (= [nil [:a-chimpanzee :give-pear :is-sad :not-eat :looks-loathingly]
                nil]
               [(nth exec-step-rets 4) (nth exec-step-rets 9)
                (nth exec-step-rets 12)])))))

(t/deftest stop-flag-works-test
  (t/testing "The ::sut/stop true flag tells the test runner to stop running
             scenarios after the first one fails."
    (let [features [(parser/parse chimpanzee-feature)]
          execution
          (ignore-out-err (sut/run features fake-registry-error {::sut/stop true}))
          exec-steps (->> execution ::sut/executables (map ::parser/steps) flatten)
          success-exec-steps (filter
                              (fn [s] (nil? (-> s ::sut/execution ::sut/err))) exec-steps)
          success-exec-step-count (count success-exec-steps)
          exec-step-rets
          (map (fn [s]
                 (get-in s [::sut/execution ::sut/ctx-after-exec :step-rets])) exec-steps)]
      ;; There is only 1 failure because the second is not allowed to happen.
      (t/is (= 12 success-exec-step-count))
      ;; The :step-rets key in the context of the end of each scenario should
      ;; be absent because no scenarios complete successfully.
      (t/is (= [nil nil nil]
               [(nth exec-step-rets 4) (nth exec-step-rets 9)
                (nth exec-step-rets 12)])))))

(t/deftest analyze-executed-executable-test
  (t/testing "That analyze-executed-executable gives the correct outcome and step
             counts."
    (t/are
        [execution expected]
        (= (-> execution
               sut/analyze-executed-executable
               (select-keys [::sut/steps-passed
                             ::sut/steps-untested
                             ::sut/steps-failed
                             ::sut/executions-passed
                             ::sut/executions-failed
                             ::sut/outcome]))
           expected)

      (first fake-run-outcome-1)
      {::sut/steps-passed 2
       ::sut/steps-untested 2
       ::sut/steps-failed 1
       ::sut/executions-passed 0
       ::sut/executions-failed 1
       ::sut/outcome :error}

      (second fake-run-outcome-1)
      {::sut/steps-passed 5
       ::sut/steps-untested 0
       ::sut/steps-failed 0
       ::sut/executions-passed 1
       ::sut/executions-failed 0
       ::sut/outcome :pass}

      (nth fake-run-outcome-1 2)
      {::sut/steps-passed 2
       ::sut/steps-untested 2
       ::sut/steps-failed 1
       ::sut/executions-passed 0
       ::sut/executions-failed 1
       ::sut/outcome :error}

      (nth fake-run-outcome-1 3)
      {::sut/steps-passed 5
       ::sut/steps-untested 0
       ::sut/steps-failed 0
       ::sut/executions-passed 1
       ::sut/executions-failed 0
       ::sut/outcome :pass}

      (first fake-run-outcome-2)
      {::sut/steps-passed 2
       ::sut/steps-untested 0
       ::sut/steps-failed 0
       ::sut/executions-passed 1
       ::sut/executions-failed 0
       ::sut/outcome :pass})))

(t/deftest produce-outcome-map-test
  (t/testing "That get-run-outcome gives the correct aggregate step and
             execution pass/fail/untested counts."
    (t/are
        [run-outcome expected]
        (= (->> run-outcome
                sut/get-run-outcome
                vals
                (map vals)
                flatten
                first)
           expected)
      fake-run-outcome-1
      {::sut/steps-passed 14
       ::sut/steps-untested 4
       ::sut/steps-failed 2
       ::sut/executions-passed 2
       ::sut/executions-failed 2}

      fake-run-outcome-2
      {::sut/steps-passed 2
       ::sut/steps-untested 0
       ::sut/steps-failed 0
       ::sut/executions-passed 1
       ::sut/executions-failed 0})))

(t/deftest summarize-run-test
  (t/testing "That summarize-run gives the correct outcome summary data
              structure for various test run outcomes."
    (let [f-a {::parser/name "A" ::parser/description "a" ::parser/tags []}
          f-b {::parser/name "B" ::parser/description "b" ::parser/tags []}
          s-a {::parser/description "a" ::parser/tags []}
          s-b {::parser/description "b" ::parser/tags []}]
      (t/are
          [run-outcome expected]
          (= expected (-> run-outcome sut/summarize-run ::sut/outcome-summary))

        ;; 2 features, each with 1 scenario; first feature fails because its
        ;; scenario fails because one of its step executions errors; second feature
        ;; passes because its sole scenario does.

        [{::parser/steps  ;; pass, pass, error, untested, untested
          [{::sut/execution {::sut/err nil}}
           {::sut/execution {::sut/err nil}}
           {::sut/execution {::sut/err {::sut/type :error}}}
           {::sut/execution nil}
           {::sut/execution nil}]
          ::sut/feature f-a
          ::sut/scenario s-a}
         {::parser/steps  ;; pass, pass
          [{::sut/execution {::sut/err nil}}
           {::sut/execution {::sut/err nil}}]
          ::sut/feature f-a
          ::sut/scenario s-a}
         {::parser/steps  ;; pass, pass
          [{::sut/execution {::sut/err nil}}
           {::sut/execution {::sut/err nil}}]
          ::sut/feature f-b
          ::sut/scenario s-b}]
        {::sut/steps-passed 6
         ::sut/steps-untested 2
         ::sut/steps-failed 1
         ::sut/scenarios-passed 1
         ::sut/scenarios-failed 1
         ::sut/features-passed 1
         ::sut/features-failed 1}))))

(comment

  (parser/parse-tag-expression-with-fallback
   "not @a or @b and not @c or not @d or @e and @f")

  (=
   (parser/parse-tag-expression-with-fallback
    "not @a or @b and not @c or not @d or @e and @f")
   '(or (or (or (not "a") (and "b" (not "c"))) (not "d")) (and "e" "f")))

  (parser/parse-tag-expression-with-fallback
   "(@chimpanzees and @orangutan) and (@chimpanzees or @orangutan)")

  (parser/parse-tag-expression-with-fallback
   "((@chimpanzees and @orangutan) and (@chimpanzees or @orangutan))")

)
