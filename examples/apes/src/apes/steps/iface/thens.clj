(ns apes.steps.iface.thens
  (:require [apes.steps.impl.core :refer [a-result
                                          not-eat-it
                                          is-adjective
                                          looks-at-me]]
            [tegere.steps :refer [Then]]))

(Then "a result" a-result)
(Then "he doesn't eat it" not-eat-it)
(Then "he is {adjective}" is-adjective)
(Then "he looks at me {adverb}" looks-at-me)
