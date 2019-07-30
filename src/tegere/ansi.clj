(ns tegere.ansi)

;; The MIT License
;; ===============
;;
;; Copyright (c) 2009-2014 Amsterdam Clojurians
;;
;; Permission is hereby granted, free of charge, to any person obtaining a copy
;; of this software and associated documentation files (the "Software"), to deal
;; in the Software without restriction, including without limitation the rights
;; to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
;; copies of the Software, and to permit persons to whom the Software is
;; furnished to do so, subject to the following conditions:
;;
;; The above copyright notice and this permission notice shall be included in
;; all copies or substantial portions of the Software.
;;
;; THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
;; IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
;; FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
;; AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
;; LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
;; OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
;; THE SOFTWARE.

(def ANSI-CODES
  {:reset              "[0m"
   :bright             "[1m"
   :blink-slow         "[5m"
   :underline          "[4m"
   :underline-off      "[24m"
   :inverse            "[7m"
   :inverse-off        "[27m"
   :strikethrough      "[9m"
   :strikethrough-off  "[29m"

   :default "[39m"
   :white   "[37m"
   :black   "[30m"
   :red     "[31m"
   :green   "[32m"
   :blue    "[34m"
   :yellow  "[33m"
   :magenta "[35m"
   :cyan    "[36m"

   :bg-default "[49m"
   :bg-white   "[47m"
   :bg-black   "[40m"
   :bg-red     "[41m"
   :bg-green   "[42m"
   :bg-blue    "[44m"
   :bg-yellow  "[43m"
   :bg-magenta "[45m"
   :bg-cyan    "[46m"})

(def ^:dynamic *use-ansi* "Rebind this to false if you don't want to see ANSI codes in some part of your code." true)

(defn ansi
  "Output an ANSI escape code using a style key.
   (ansi :blue)
   (ansi :underline)
  Note, try (style-test-page) to see all available styles.
  If *use-ansi* is bound to false, outputs an empty string instead of an
  ANSI code. You can use this to temporarily or permanently turn off
  ANSI color in some part of your program, while maintaining only 1
  version of your marked-up text.
  "
  [code]
  (if *use-ansi*
    (str \u001b (get ANSI-CODES code (:reset ANSI-CODES)))
    ""))

(defn style
  "Applies ANSI color and style to a text string.
   (style \"foo\" :red)
   (style \"foo\" :red :underline)
   (style \"foo\" :red :bg-blue :underline)
 "
  [s & codes]
  (str (apply str (map ansi codes)) s (ansi :reset)))
