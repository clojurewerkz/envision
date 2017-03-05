(defproject clojurewerkz/envision "0.1.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src"]
  :resource-paths ["resources" "cljs"]
  :dependencies [[org.clojure/clojure      "1.5.1"]
                 [enlive                   "1.1.5"]
                 [me.raynes/fs             "1.4.4"]
                 [cheshire                 "5.3.1"]
                 [prismatic/schema         "0.2.2"]
                 [clojurewerkz/statistiker "0.1.0-SNAPSHOT"]
                 [commons-io/commons-io    "2.4"]]


  :repositories {"sonatype"               {:url "http://oss.sonatype.org/content/repositories/releases"
                                           :snapshots false
                                           :releases {:checksum :fail}}
                 "sonatype-snapshots"     {:url "http://oss.sonatype.org/content/repositories/snapshots"
                                           :snapshots true
                                           :releases {:checksum :fail :update :always}}})
