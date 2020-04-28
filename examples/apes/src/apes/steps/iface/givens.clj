(ns apes.steps.iface.givens
  (:require [tegere.steps :refer [Given]]
            [apes.steps.impl.core :refer [an-animal all-good]]))

(Given "a {animal}" an-animal)
(Given "everything is all good" all-good)
