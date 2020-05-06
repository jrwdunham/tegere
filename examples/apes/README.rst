================================================================================
  The Features of Apes
================================================================================

This is an example TeGere Gherkin feature-running application. It ensures that
our ape cousins behave with decorum.


Run from a REPL
================================================================================

Fire up an nREPL server at localhost:8999::

    clj -A:dev

Connect to it from your editor and execute some of the commented out calls to
``apes.core/-main``. This should result in your standard output stream
containing an ANSI-colored representation of the running of the feature,
including step execution times::

    @chimpanzees
    Feature: Chimpanzees behave as expected
      Experimenters want to ensure that their chimpanzee simulations are behaving correctly.

      @fruit-reactions
      Scenario: Chimpanzees behave as expected when offered various foods.

        Given a chimpanzee (took 0.0s)
        When I give him a banana (took 0.0s)
        Then he is happy (took 0.0s)
        But he doesn't eat it (took 0.0s)
        And he looks at me quizzically (took 0.0s)

The return value of the function is a data structure representing a record of the
feature execution:

.. code-block:: clojure

       #:tegere.runner{:outcome-summary
                       #:tegere.runner{:steps-passed 26,
                                       :steps-untested 0,
                                       :steps-failed 0,
                                       :scenarios-passed 4,
                                       :scenarios-failed 0,
                                       :features-passed 2,
                                       :features-failed 0},
                       :outcome-summary-report
                       "\n2 features passed, 0 failed\n4 scenarios passed, 0 failed\n26 steps passed, 0 failed, 0 untested\n",
                       :executables
                       ({:tegere.parser/steps
                         ({:tegere.parser/type :given,
                           :tegere.parser/text "a chimpanzee",
                           :tegere.runner/fn
                           #function[tegere.runner/get-step-fn/fn--11332/fn--11336],
                           :tegere.runner/execution
                           #:tegere.runner{:start-time
                                           #inst "2020-05-04T22:44:58.484-00:00",
                                           :end-time
                                           #inst "2020-05-04T22:44:58.484-00:00",
                                           :ctx-after-exec
                                           {:config
                                            {:tegere.runner/stop true,
                                             :tegere.runner/verbose false,
                                             :tegere.runner/data {},
                                             :tegere.query/query-tree nil,
                                             :tegere.runner/features-path
                                             "src/apes/features"},
                                            :step-rets [:a-chimpanzee]},
                                           :err nil}} ...)}...)}


Run from the Command Line
================================================================================

Using the ``clj`` tool::

    clj -m apes.core src/apes/features/

This should print out the features that were run, as well as a summary of
execution results::

    2 features passed, 0 failed
    4 scenarios passed, 0 failed
    26 steps passed, 0 failed, 0 untested

To control which scenarios are executed, supply one or more ``--tags`` options.
These contain boolean tag expression queries over tags specified in the feature
files. The following example will run only those two scenarios where a chimp
receives a banana or a bonobo is interacting with an orangutan::

    $ clj -m apes.core src/apes/features/ \
          --tags='@chimpanzees and @fruit=banana or @bonobos and @orangutan'
    2 features passed, 0 failed
    2 scenarios passed, 0 failed
    8 steps passed, 0 failed, 0 untested

Note that the tag search expression grammar gives operator precedence to
disjunction over conjunction, so the above tag expression is equivalent to::

    (@chimpanzees and @fruit=banana) or (@bonobos and @orangutan)
