(defproject clojure-by-example/kern "0.1.0-SNAPSHOT"
  :description "parser kern By Example"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.blancas/kern "0.7.0"]
                 [enlive "1.1.1"]
                 [net.cgrand/moustache "1.2.0-alpha2"]
                 [ring "1.1.8"]]
  :uberjar-name "kern-by-example.jar"
  :main example.kern.web)
