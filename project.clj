(defproject clojure-by-example "1.0.0-SNAPSHOT"
  :description "Web Apps to play with clojure ecosystem : enlive, core.logic..."
  :url "https://github.com/maxrzepka/clojure-by-example"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:url "git@github.com:maxrzepka/clojure-by-example.git"}
  :sub ["enlive" "core.logic" "cascalog"]
  :plugins [[lein-sub "0.2.4"]]
  :aliases {"clean" ["sub" "clean"]})
