================================================================================
  TeGere
================================================================================

A Gherkin library for Clojure. The name "te gere" is intended to be a Latin
translation of English "behave!". Modeled after `Python Behave`_.

Yes, I do know about `cucumber-jvm`_ and `cucumber-js`_. TeGere offers a
Clojure-first Gherkin library and an opportunity to use Instaparse_ to create a
useful DSL.


Usage
================================================================================

Run TeGere with the ``clj`` tool against the examples/ directory, which contains
sample Gherkin feature files and step implementations::

    $ clj -A:run examples/
    2 features passed, 0 failed
    4 scenarios passed, 0 failed
    26 steps passed, 0 failed, 0 untested 

The same can be accomplished with Leiningen::

    $ lein run examples/

Alternatively, build a JAR and run it against examples/::

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
    
or Leiningen::

    $ lein test


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
