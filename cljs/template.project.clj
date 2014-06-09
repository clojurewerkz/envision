(defproject template "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojurescript "0.0-2197"
                  :exclusions [org.apache.ant/ant]]
                 [reagent "0.4.2"
                  :exclusions [org.clojure/clojurescript]]
                 [cljs-ajax "0.2.2"]
                 [clojurewerkz/balagan "1.0.0"]
                 [prismatic/schema "0.2.2"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :cljsbuild {:builds [{:source-paths ["./src"]
                        :compiler {:output-to "../assets/cljs/main.js"
                                   :output-dir "../assets/cljs"
                                   :optimizations :none
                                   :source-map true
                                   :pretty-print true
                                   }}]})
