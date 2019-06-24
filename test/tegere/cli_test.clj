(ns tegere.cli-test
  (:require [tegere.cli :as sut]
            [clojure.test :as t]))

(t/deftest command-line-parsing-test
  (t/testing "We can parse command-line arguments"
    (let [parse1
          (sut/simple-cli-parser
           (list "/path/to/dir/" "--opt=val" "-opt-2=val" "--flag"))
          parse2
          (sut/simple-cli-parser
           (list "/path/to/dir/" "--tags=monkeys" "--tags=behaviour"))
          parse3
          (sut/simple-cli-parser
           (list "/path/to/dir/" "--tags=monkeys" "--tags=behaviour"
                 "--tags=a,b,c"))
          parse4
          (sut/simple-cli-parser
           (list "/path/to/dir/" "second pos arg" "--tags=a,b,c"
                 "--stop"))]
      (do
        (t/is (= parse1
                 {:args ["/path/to/dir/"]
                  :kwargs {:opt "val" :opt-2 "val" :flag true}}))
        (t/is (= parse2
                 {:args ["/path/to/dir/"]
                  :kwargs {:and-tags #{"monkeys" "behaviour"}}}))
        (t/is (= parse3
                 {:args ["/path/to/dir/"]
                  :kwargs {:and-tags #{"monkeys" "behaviour"}
                           :or-tags #{"a" "b" "c"}}}))
        (t/is (= parse4
                 {:args ["/path/to/dir/" "second pos arg"]
                  :kwargs {:or-tags #{"a" "b" "c"} :stop true}}))))))
