# TeGere

A Gherkin library for Clojure. The name "te gere" is intended to be a Latin
translation of English "behave!". Modeled after the Python Behave library.

Yes, I do know about [cucumber-jvm](https://github.com/cucumber/cucumber-jvm)
and [cucumber-js](https://github.com/cucumber/cucumber-js). This is mostly
just for fun. It's especially fun if you're looking for an excuse to use
[Instaparse](https://github.com/Engelberg/instaparse).


## Usage

Run TeGere with Leiningen against the examples directory, which contains sample
Gherkin feature files and step implementations:

    $ lein run examples/

Alternatively, build a JAR and run it against examples/

    $ lein uberjar
    $ java -jar target/uberjar/tegere-0.1.0-SNAPSHOT-standalone.jar examples/


## Run the Tests

Use leiningen to run the tests:

    $ lein test


## License

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
