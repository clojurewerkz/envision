(defproject clojurewerkz/envisioncljs "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2173"
                  :exclusions [org.apache.ant/ant]]
                 [reagent "0.4.2"
                  :exclusions [org.clojure/clojurescript]]
                 ;; [cljs-ajax "0.2.2"]
                 [clojurewerkz/balagan "1.0.0" :exclusions [org.clojure/clojure]]
                 [prismatic/schema "0.2.4"]]

  :plugins [[lein-cljsbuild "1.0.2"]]

  :cljsbuild {:builds [{:source-paths ["./src"]
                        :compiler {:output-to "../resources/assets/cljs/main.js"
                                   :output-dir "../resources/assets/cljs"
                                   :optimizations :none
                                   :source-map true
                                   :pretty-print true
                                   }}]})
