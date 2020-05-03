================================================================================
  TeGere
================================================================================

A Gherkin library for Clojure. To "te gere" is to carry oneself with dignity or,
as the vulgar crowd might say, to "behave!".

    "Now, if you have one of these pretend testing systems that lets you write
    English strings so that the customer can look at it, that's just silly."

    -- Rich Hickey (Simple Made Easy talk)

    "You know a fool who persists in his folly becomes wise."

    -- Alan Watts

Yes, `cucumber-jvm`_ and `cucumber-js`_ exist. TeGere offers a Clojure-first
Gherkin library and an opportunity to use Instaparse_ to create a useful DSL.
Takes inspiration from `Python Behave`_.


Quickstart
================================================================================

Add the following line to the `:deps` map of your deps.edn:

.. code-block:: clojure

       tegere
       {:git/url "https://github.com/jrwdunham/tegere"
        :sha "0bf666e891165b11073bd24c2a9f2851ee33afe3"}}

Then write some Gherkin_ feature files and save them (with the `.feature`
extension) to some directory. Now map the Gherkin step text to Clojure functions
using the ``Given``, ``When`` and ``Then`` functions of ``tegere.steps``.
Finally, execute the features by calling:

.. code-block:: clojure

       user> (tegere.runner/run
               (tegere.loader/load-feature-files "path/to/gherkin")
               @tegere.steps/registry)

An optional config map may be passed to ``run`` as a third argument. It
recognizes the boolean key ``tegere.runner/stop`` which will cause TeGere to stop
feature execution after the first failure, and ``:tegere.query/query-tree`` which
is a boolean search tree (see the spec) that controls which scenarios get
executed::

.. code-block:: clojure

       user> (tegere.runner/run
               (tegere.loader/load-feature-files "path/to/gherkin")
               @tegere.steps/registry
               {:tegere.runner/stop true
                :tegere.query/query-tree
                '(or (and "chimpanzees" (not "fruit-reactions"))
                     "bonobos")})

For additional documentation, see the :ref:`Detailed Usage` section or the
example Apes_ project under the `examples`/ folder.


Detailed Usage
================================================================================

Consider the simplistic Gherkin feature files under
`examples/chimps/chimps.feature`::

    @chimpanzees
    Feature: Chimpanzees behave as expected
      Experimenters want chimpanzee sims to behave correctly.

      @fruit-reactions
      Scenario: Chimpanzees behave as expected when offered various foods.
        Given a chimpanzee
        When I give him a papaya
        Then he is happy

To parse and load this feature file into a Clojure data structure, pass its
directory path to ``tegere.loader/load-feature-files``:

.. code-block:: clojure

       user> (require '[tegere.loader :refer [load-feature-files]])
       user> (def features (load-feature-files "examples/chimps"))
       user> features
       [#:tegere.parser{:name "Chimpanzees behave as expected",
                        :description
                        "Experimenters want chimpanzee sims to behave correctly.",
                        :tags ["chimpanzees"],
                        :scenarios
                        [#:tegere.parser{:description
                                         "Chimpanzees behave as expected when ...",
                                         :tags ["fruit-reactions"],
                                         :steps
                                         [#:tegere.parser{:type :given,
                                                          :text "a chimpanzee"}
                                          #:tegere.parser{:type :when,
                                                          :text "I give him a papaya"}
                                          #:tegere.parser{:type :then,
                                                          :text "he is happy"}]}]}]

The loaded feature is a ``::tegere.parser/features`` collection of
``::tegere.parser/feature`` maps.

Now we can use the appropriate step function (``Given``, ``When``, or ``Then``)
to populate the global steps registry atom that maps regular expressions
(strings) matching Gherkin Step statements to Clojure functions:

.. code-block:: clojure

       user> (require '[tegere.steps :refer [registry Given When Then]])
       user> (Given "a {animal}" (fn [ctx animal] (assoc ctx :animal animal)))
       user> (When "I give him a {fruit}"
                   (fn [ctx fruit]
                     (merge ctx
                            {:received fruit
                             :emotion (if (= fruit "pear") "happy" "sad")})))
       user> (Then "he is {emotion}"
                   (fn [{actual-emotion :emotion :as ctx} emotion]
                     (assert (= emotion actual-emotion)
                             (format "Ape is %s, expected her to be %s."
                                     actual-emotion emotion))))
       user> @registry
       {:given {"a {animal}" #function[user/eval13631/fn--13632]},
        :when {"I give him a {fruit}" #function[user/eval13641/fn--13642]},
        :then {"he is {emotion}" #function[user/eval13645/fn--13646]}}

Finally, call ``tegere.runner/run`` to execute the parsed features using the
populated registry. The optional third argument to ``run`` is a config map: setting
``:stop`` on this map to ``true`` will cause test execution to halt after the
first failure. The ``:tags`` key may also contain ``:and-tags`` and/or
``:or-tags`` keys, whose values are sets of strings. The scenarios that are
ultimately run are those that match all of the *and* tags and at least one of the
*or* tags:

.. code-block:: clojure

       user> (require '[tegere.runner :refer [run]])
       user> (run features @registry)
       @chimpanzees
       Feature: Chimpanzees behave as expected
         Experimenters want chimpanzee sims to behave correctly.

         @fruit-reactions
         Scenario: Chimpanzees behave as expected when offered various foods.

           Given a chimpanzee (took 0.0s)
           When I give him a papaya (took 0.0s)
           Then he is happy (took 0.001s)
               Assertion error: Assert failed: Ape is sad, expected her to be happy.
                   (= emotion actual-emotion)

       0 features passed, 1 failed
       0 scenarios passed, 1 failed
       2 steps passed, 1 failed, 0 untested

Above is shown the text that is written to stdout when this feature is executed.
The return value of ``run`` is a step execution map detailing how long it took to
execute each step and whether the step passed or failed.

TODO: document the ``tegere.cli`` namespace, once it is complete.


Notes (TODO: edit/process)
================================================================================

Run TeGere with the ``clj`` tool against the examples/ directory, which contains
sample Gherkin feature files and step implementations:

.. code-block:: bash

       $ clj -A:run examples/
       2 features passed, 0 failed
       4 scenarios passed, 0 failed
       26 steps passed, 0 failed, 0 untested

The same can be accomplished with Leiningen:

.. code-block:: bash

       $ lein run examples/

Alternatively, build a JAR and run it against examples/:

.. code-block:: bash

       $ lein uberjar
       $ java -jar target/uberjar/tegere-0.1.0-SNAPSHOT-standalone.jar examples/

Example usage in a Clojure project:

.. code-block:: clojure

       (ns example.core
         (:require [tegere.cli :as tegcli]
                   [tegere.loader :as tegload]
                   [tegere.runner :as tegrun]
                   [tegere.steps :as tegstep]
                   [example.steps.core]))  ;; should register step functions

       (defn main
         [args]
         (let [cli-args (tegcli/simple-cli-parser args)
               config {:tags (select-keys (:kwargs cli-args) [:and-tags :or-tags])
                       :stop (get-in cli-args [:kwargs :stop] false)}
               features (tegload/load-feature-files (-> cli-args :args first))]
           (tegrun/run features @tegstep/registry config)))

       (defn -main
         [& args]
         (println (main args)))


Run the Tests
================================================================================

Use the ``test`` alias defined in ``deps.edn``::

    $ clj -A:test

To run tests specific to a single namespace, e.g., ``tegere.grammar``::

    $ clj -A:test -n tegere.grammar-test

To run a specific ``deftest``::

    $ clj -A:test -n tegere.runner-test -v tegere.runner-test/can-run-simple-feature-test


License
================================================================================

Copyright © 2019 Joel Dunham

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.


.. _`cucumber-js`: https://github.com/cucumber/cucumber-js
.. _`cucumber-jvm`: https://github.com/cucumber/cucumber-jvm
.. _`Python Behave`: https://github.com/behave/behave
.. _Instaparse: https://github.com/Engelberg/instaparse
.. _Gherkin: https://cucumber.io/docs/gherkin/reference/
.. _Apes: examples/apes/README.rst
