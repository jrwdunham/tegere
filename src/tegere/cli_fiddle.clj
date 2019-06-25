(ns tegere.cli-fiddle
  (:require [tegere.cli :refer :all]
            [clojure.string :as s]))

(comment

  (simple-cli-parser (list "/path/to/dir/" "--opt=val" "-opt-2=val" "--flag"))

  (simple-cli-parser (list "/path/to/dir/" "--tags=monkeys" "--tags=behaviour"))

  (simple-cli-parser (list "/path/to/dir/" "--tags=monkeys" "--tags=behaviour" "--tags=a,b,c"))

  (simple-cli-parser (list "/path/to/dir/" "second pos arg" "--tags=a,b,c" "--stop"))

  (simple-cli-parser (list "run" "--tags=monkeys" "0" "--stop" "-D=DdD"))

  (parse-arg "run")

  (parse-arg "--tags=monkeys")

  (parse-arg "--stop")

  (parse-arg "")

)
