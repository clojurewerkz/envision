(defproject clojurewerkz/envision "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clojurewerkz/gizmo "1.0.0-alpha4-SNAPSHOT"]
                 [org.clojure/tools.nrepl "0.2.3"]

                 [clj-time "0.6.0"]

                 [clojurewerkz/statistiker "0.1.0-SNAPSHOT"]
                 [me.raynes/fs "1.4.4"]
                 [org.clojure/tools.cli "0.2.2"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.4"]]}}
  :source-paths ["src"]
  :resource-paths ["resources"]
  :main envision.cli-entrypoint)
