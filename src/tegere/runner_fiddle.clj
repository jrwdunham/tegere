(ns tegere.runner-fiddle
  "Fiddle file for playing around with runner.clj."
  (:require [tegere.runner :refer :all]
            [tegere.parser :refer [parse]]
            [tegere.grammar-fiddle :refer [monkey-feature]]))

(defn update-step-rets
  "Convenience fiddle function that appends val to the :step-rets key of the map
  context, while ensuring that the val of :step-rets is a vec."
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
   :when {"I give him a banana" (fn [context] (update-step-rets context :give-banana))
          "I give him a pear" (fn [context] (update-step-rets context :give-pear))}
   :then {"he doesn't eat it" (fn [context] (update-step-rets context :not-eat))
          "he is happy" (fn [context] (update-step-rets context (/ 1 0)))
          "he is sad" (fn [context] (update-step-rets context :is-sad))
          "he looks at me loathingly"
          (fn [context] (update-step-rets context :looks-loathingly))
          "he looks at me quizzically"
          (fn [context] (update-step-rets context :looks-quizzically))}})

(defn for-repl
  "Call this in a REPL to see how printing to stdout works."
  [& {:keys [stop?] :or {stop? false}}]
  (let [features [(parse monkey-feature) (parse monkey-feature)]
        config {:tags {:and-tags #{"monkeys" "fruit-reactions"}}
                :stop stop?}
        fake-registry
        {:given {"a monkey" (fn [context] (update-step-rets context :a-monkey))}
         :when {"I give him a banana"
                (fn [context] (update-step-rets context :give-banana))
                "I give him a pear"
                (fn [context] (update-step-rets context :give-pear))}
         :then {"he doesn't eat it"
                (fn [context] (assert (= :not-eat true)
                                      "He DOES eat it you fool!"))
                "he is happy" (fn [context] (update-step-rets context (/ 1 0)))
                "he is sad" (fn [context] (update-step-rets context :is-sad))
                "he looks at me loathingly"
                (fn [context] (update-step-rets context :looks-loathingly))
                "he looks at me quizzically"
                (fn [context] (update-step-rets context :looks-quizzically))}}]
    (run features fake-registry config)))

;; Fake seq of scenario executions
(def fake-run-outcome
  [{:steps
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
      :text "he looks at me quizzically"
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
   {:steps
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
   {:steps
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
      :text "he looks at me quizzically"
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
   {:steps
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

(def other-fake-run-outcome
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

;; Like runner_test::fake-run-outcome-3 but split across two scenarios in one feature.
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
      :text "he looks at me quizzically"
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
      :text "he looks at me quizzically"
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

;; 
(def minimal-run-outcome
  [{:steps  ;; pass, pass, error, untested, untested
    [{:execution {:err nil}}
     {:execution {:err nil}}
     {:execution {:err {:type :error}}}
     {:execution nil}
     {:execution nil}]
    :feature "f-a"
    :scenario "s-a"}
   ])


(comment

  ((juxt :feature :scenario :outcome) (first other-fake-run-outcome))

  (let [[a b] (if false [1 2] [3 4])]
    [a b])

  (->> other-fake-run-outcome
       first
       analyze-step-execution
       ((juxt :feature :scenario :outcome)))

  ;; {{:name "the porkcase monkey integration liquidity endpoint works",
  ;;   :description
  ;;   "porkcase wants to ensure that requests to the monkey integration liquidity endpoint are handled correctly.",
  ;;   :tags ("monkey" "liquidity")}
  ;;  {{:description
  ;;    "Well-formed update requests to the monkey liquidity endpoint are handled correctly.",
  ;;    :tags ("update")}
  ;;   {:step-pass-count 2,
  ;;    :step-untested-count 0,
  ;;    :step-fail-count 0,
  ;;    :execution-pass-count 1,
  ;;    :execution-fail-count 0}}}

  (->> other-fake-run-outcome
       executions->outcome-map
       outcome-map->outcome-summary-map
       )

  ;; Input:
  ;; {{:name "Monkeys behave as expected",
  ;;   :description
  ;;   "Experimenters want to ensure that their monkey simulations are behaving correctly.",
  ;;   :tags ("monkeys")}
  ;;  {{:description "A", :tags ("a")}
  ;;   {:step-pass-count 7,
  ;;    :step-untested-count 2,
  ;;    :step-fail-count 1,
  ;;    :execution-pass-count 1,
  ;;    :execution-fail-count 1},
  ;;   {:description "B", :tags ("b")}
  ;;   {:step-pass-count 7,
  ;;    :step-untested-count 2,
  ;;    :step-fail-count 1,
  ;;    :execution-pass-count 1,
  ;;    :execution-fail-count 1}}}
  ;; Current output:
  ;; {:features {:passed 1, :failed 0},
  ;;  :scenarios {:passed 2, :failed 0},
  ;;  :steps {:passed 24, :failed 0, :untested 0}}
  (->> fake-run-outcome-3
       executions->outcome-map
       outcome-map->outcome-summary-map
       )

  (let [scenarios
        {{:description "A" :tags (list "a")}
         {:step-pass-count 7
          :step-untested-count 2
          :step-fail-count 1
          :execution-pass-count 1
          :execution-fail-count 1}
         {:description "B" :tags (list "b")}
         {:step-pass-count 7
          :step-untested-count 2
          :step-fail-count 1
          :execution-pass-count 1
          :execution-fail-count 1}}]
    (apply (partial merge-with + {:passed 0 :failed 0})
           (->> scenarios
                vals
                (map (fn [scen-val]
                       (println "scen-val")
                       (println scen-val)
                       (reduce
                        (fn [agg [k v]]
                          (let [new-k (if (some #{k} [:error :fail])
                                        :failed :passed)]
                            (merge-with + agg {new-k v})))
                        {:passed 0 :failed 0}
                        scen-val))))))

  (let [scenarios
        {{:description "A" :tags (list "a")}
         {:step-pass-count 7
          :step-untested-count 2
          :step-fail-count 1
          :execution-pass-count 1
          :execution-fail-count 1}
         {:description "B" :tags (list "b")}
         {:step-pass-count 7
          :step-untested-count 2
          :step-fail-count 1
          :execution-pass-count 1
          :execution-fail-count 1}}]
    (apply merge-with + (vals scenarios)))

  (let [scenarios
        {{:description "A" :tags (list "a")}
         {:step-pass-count 7
          :step-untested-count 2
          :step-fail-count 1
          :execution-pass-count 1
          :execution-fail-count 1}
         {:description "B" :tags (list "b")}
         {:step-pass-count 7
          :step-untested-count 2
          :step-fail-count 1
          :execution-pass-count 1
          :execution-fail-count 1}}]
    (->> scenarios
         vals
         (map (fn [step-stats-map]
                (if (= 0 (:execution-fail-count step-stats-map))
                  {:scenario-pass-count 1
                   :scenario-fail-count 0}
                  {:scenario-pass-count 0
                   :scenario-fail-count 1})))
         (apply merge-with +)))

  (let [scenarios
        {{:description "A" :tags (list "a")}
         {:step-pass-count 7
          :step-untested-count 2
          :step-fail-count 1
          :execution-pass-count 1
          :execution-fail-count 1}
         {:description "B" :tags (list "b")}
         {:step-pass-count 7
          :step-untested-count 2
          :step-fail-count 1
          :execution-pass-count 1
          :execution-fail-count 1}}
        steps-stats (apply merge-with + (vals scenarios))
        scenarios-stats
        (->> scenarios
             vals
             (map (fn [step-stats-map]
                    (if (= 0 (:execution-fail-count step-stats-map))
                      {:scenario-pass-count 1
                       :scenario-fail-count 0}
                      {:scenario-pass-count 0
                       :scenario-fail-count 1})))
             (apply merge-with +))
        feature-stats
        (if (= 0 (:scenario-fail-count scenarios-stats))
               {:feature-pass-count 1
                :feature-fail-count 0}
               {:feature-pass-count 0
                :feature-fail-count 1})]
    (merge steps-stats
           scenarios-stats
           feature-stats))

  (executions->outcome-map other-fake-run-outcome)

  (analyze-step-execution (first other-fake-run-outcome))

  (executions->outcome-map fake-run-outcome)

  (analyze-step-execution (first fake-run-outcome))

  (analyze-step-execution (second fake-run-outcome))

  (analyze-step-execution (nth fake-run-outcome 2))

  (analyze-step-execution (nth fake-run-outcome 3))

  (get-outcome-summary minimal-run-outcome)

  (->
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
   (get-outcome-summary :as-data? false))

  (sort {:a 2 :b 3})

  (select-keys)

  (get-outcome-summary fake-run-outcome)

  (get-outcome-summary other-fake-run-outcome)

  (get-outcome-summary fake-run-outcome-3)

  (get-step-fn-args "I ate a {fruit-type}" "I ate a banana")

  (get-step-fn-args "I ate a banana" "I ate a pear")

  (get-step-fn-args "I ate a pear" "I ate a pear")

  ((get-step-fn fake-registry {:type :when :text "I give him a pear"}) {})

  ((get-step-fn fake-registry {:type :when :text "I give him a banana"}) {})

  ((get-step-fn fake-registry {:type :when :text "I give him a pear"}) {})

  (let [features [(parse monkey-feature) (parse monkey-feature)]
        config {:tags {:and-tags #{"monkeys" "fruit-reactions"}}
                :stop false}
        fake-registry
        {:given {"a monkey" (fn [context] (update-step-rets context :a-monkey))}
         :when {"I give him a banana" (fn [context] (update-step-rets context :give-banana))
                "I give him a pear" (fn [context] (update-step-rets context :give-pear))}
         :then {"he doesn't eat it" (fn [context] (update-step-rets context :not-eat))
                ;"he is happy" (fn [context] (update-step-rets context :is-happy))
                "he is happy" (fn [context] (update-step-rets context (/ 1 0)))
                "he is sad" (fn [context] (update-step-rets context :is-sad))
                "he looks at me loathingly"
                (fn [context] (update-step-rets context :looks-loathingly))
                "he looks at me quizzically"
                (fn [context] (update-step-rets context :looks-quizzically))}}]
    (run features fake-registry config))

  (let [features [(parse monkey-feature) (parse monkey-feature)]
        config {:tags {:and-tags #{"monkeys" "fruit-reactions"}}
                :stop false}
        fake-registry
        {:given {"a monkey" (fn [context] (update-step-rets context :a-monkey))}
         :when {"I give him a {fruit}"
                (fn [context fruit]
                  (update-step-rets context (keyword (format "give-with-var-%s" fruit))))}
         :then {"he doesn't eat it" (fn [context] (update-step-rets context :not-eat))
                ;"he is happy" (fn [context] (update-step-rets context :is-happy))
                "he is happy" (fn [context] (update-step-rets context (/ 1 0)))
                "he is sad" (fn [context] (update-step-rets context :is-sad))
                "he looks at me loathingly"
                (fn [context] (update-step-rets context :looks-loathingly))
                "he looks at me quizzically"
                (fn [context] (update-step-rets context :looks-quizzically))}}]
    (run features fake-registry config))

  (get-step-fn-args
   "I give {recipient} a {fruit}"
   "I give him a big ole banana")  ;; ("him" "big ole banana")

  (get-step-fn-args
   "I gave {recipient} a {fruit}"
   "I give him a big ole banana")  ;; => nil

  (get-step-fn-args
   "I give him a big ole banana"
   "I give him a big ole banana")  ;; => ()

)
