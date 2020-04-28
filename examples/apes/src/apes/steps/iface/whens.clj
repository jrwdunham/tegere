(ns apes.steps.iface.whens
  (:require [tegere.steps :refer [When]]
            [apes.steps.impl.core :refer [an-action
                                          give-thing
                                          present-orangutan]]))

(When "an action" an-action)
(When "I give him a {noun}" give-thing)
(When "I present him with an orangutan" present-orangutan)
