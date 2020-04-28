(ns tegere.fiddle.cli
  (:require [tegere.cli :as cli]))

(comment

  (cli/simple-cli-parser
   (list "/path/to/dir/" "--opt=val" "-opt-2=val" "--flag"))

  (cli/simple-cli-parser
   (list "/path/to/dir/" "--tags=chimpanzees" "--tags=behaviour"))

  (cli/simple-cli-parser
   (list "/path/to/dir/" "--tags=chimpanzees" "--tags=behaviour" "--tags=a,b,c"))

  (cli/simple-cli-parser
   (list "/path/to/dir/" "second pos arg" "--tags=a,b,c" "--stop"))

  (cli/simple-cli-parser
   (list "run" "--tags=chimpanzees" "0" "--stop" "-D=DdD"))

  (cli/parse-arg "run")

  (cli/parse-arg "--tags=chimpanzees")

  (cli/parse-arg "--stop")

  (cli/parse-arg "")

)
