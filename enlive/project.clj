(defproject clojure-by-example/enlive "1.0.0-SNAPSHOT"
  :description "Web App to play with enlive"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring "1.1.8"]
                 [enlive "1.1.1"]
                 [net.cgrand/moustache "1.2.0-alpha2"]]
  ;;:source-paths ["src" "../common/src"]
  :uberjar-name "enlive-by-example.jar"
  :main example.enlive.web)
