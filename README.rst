================================================================================
  TeGere
================================================================================

.. image:: https://circleci.com/gh/jrwdunham/tegere.svg?style=svg
    :target: https://circleci.com/gh/jrwdunham/tegere
.. image:: https://img.shields.io/clojars/v/tegere.svg
    :target: https://clojars.org/tegere

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

The latest version on Clojars:

.. code-block:: bash

      {tegere {:mvn/version "0.1.5"}}

Try it out quickly:

.. code-block:: bash

      clj -Sdeps "{:deps {tegere {:mvn/version \"0.1.5\"}}}"

Now write some Gherkin_ feature files and save them (with the ``.feature``
extension) to some directory. Now map the Gherkin step strings to Clojure
functions using the ``Given``, ``When`` and ``Then`` functions of
``tegere.steps``. Finally, execute the features by calling:

.. code-block:: clojure

       user> (tegere.runner/run
               (tegere.loader/load-feature-files "path/to/gherkin")
               @tegere.steps/registry)

An optional config map may be passed to ``run`` as a third argument. It
recognizes the boolean key ``tegere.runner/stop`` which will cause TeGere to stop
feature execution after the first failure, and ``:tegere.query/query-tree`` which
is a boolean search tree (see the spec_) that controls which scenarios get
executed:

.. code-block:: clojure

       user> (tegere.runner/run
               (tegere.loader/load-feature-files "path/to/gherkin")
               @tegere.steps/registry
               {:tegere.runner/stop true
                :tegere.query/query-tree
                '(or (and "chimpanzees" (not "fruit-reactions"))
                     "bonobos")})

For additional documentation, see the ``Detailed Usage`` section below or the
example Apes_ project under the ``examples/`` folder.


Detailed Usage
================================================================================


Create and Load Gherkin Files
--------------------------------------------------------------------------------

Consider the following simplistic Gherkin feature file at
``examples/chimps/chimps.feature``::

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

The loaded feature is a ``:tegere.parser/features`` collection of
``:tegere.parser/feature`` maps.


Map Gherkin Step Definitions to Clojure Step Functions
--------------------------------------------------------------------------------

Now we can use the appropriate step-mapping function (``Given``, ``When``, or
``Then``) to populate the global steps registry atom that maps regular
expressions (strings) matching Gherkin Step statements to Clojure functions:

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

The first argument to a step function is a context map, ``ctx`` in the examples
above. If successful, the step function should return a (possibly updated)
context map. If the step fails, it should throw an exception.


Run the Features from the REPL
--------------------------------------------------------------------------------

Finally, call ``tegere.runner/run`` to execute the parsed features using the
populated registry:

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

As illustrated above, the execution of features entails a side-effect: the
outcome of the execution is written to stdout. The return value of ``run``, on
the other hand, is data: a ``:tegere.runner/run`` data structure that details
how long each step took, the context value it returned, and whether it passed or
failed.

An optional third argument (a configuration map) may be passed to ``run``.
Setting the boolean key ``:tegere.runner/stop`` to ``true`` will cause TeGere to
stop feature execution after the first failure. The value of
``:tegere.query/query-tree``, if supplied, must be a boolean search tree (see the
spec_); it controls which scenarios get executed.


Create a Command-line Interface
--------------------------------------------------------------------------------

The ``tegere.cli`` namespaces contains the ``validate-args`` function, which
can be used to create a command-line interface to a TeGere feature runner. For
example:

.. code-block:: clojure

       (cli/validate-args
         ["src/apes/features"
          "--tags=@bonobos or @chimpanzees"
          "--tags=not @orangutan"
          "-Durl=http://api.example.com"
          "--data=password=secret"
          "--stop"
          "--verbose"])
       {:tegere.runner/stop true,
        :tegere.runner/verbose true,
        :tegere.runner/data {:url "http://api.example.com", :password "secret"},
        :tegere.query/query-tree (and (not "orangutan") (or "bonobos" "chimpanzees")),
        :tegere.runner/features-path "src/apes/features"}

In the Apes_ example application, the above allows us to run the features from the
command-line using the ``clj`` tool and a command like the following:

.. code-block:: bash

       $ clj -m apes.core src/apes/features/ \
             --tags='@chimpanzees & @fruit=banana or @bonobos and @orangutan'

See the ``apes.core`` namespace in the Apes_ examples app for more details.


Run the Tests
================================================================================

Use the ``test`` alias defined in ``deps.edn``:

.. code-block:: bash

       $ clj -A:test

To run tests specific to a single namespace, e.g., ``tegere.grammar``:

.. code-block:: bash

       $ clj -A:test -n tegere.grammar-test

Examples of running specific ``deftest`` expressions:

.. code-block:: bash

       $ clj -A:test -n tegere.runner-test -v tegere.runner-test/can-run-simple-feature-test
       $ clj -A:test -n tegere.grammar-test -v tegere.grammar-test/step-block-parse-test
       $ clj -A:test -n tegere.parser-test -v tegere.parser-test/step-data-and-linebreaks
       $ clj -A:test -n tegere.parser-test -v tegere.parser-test/parsed-features-conform-to-spec


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
.. _spec: src/tegere/query.clj
