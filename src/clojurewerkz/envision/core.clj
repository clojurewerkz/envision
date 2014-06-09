(ns clojurewerkz.envision.core
  (:require [me.raynes.fs                       :as fs]
            [cheshire.core                      :as json]
            [clojure.java.io                    :as io]
            [schema.core                        :as s]
            [clojurewerkz.envision.chart-config :as cfg]

            [clojure.java.browse :refer [browse-url]]))


(def test-data (into [] (for [i (range 0 20)] {:year (+ 2000 i) :income (+ 10 i)})))

(defn prepare-tmp-dir
  "Prepares a tmp directory with all templates and returns a path to it"
  [data]
  (let [temp-dir (fs/temp-dir "envision-")
        path     (.getPath temp-dir)
        index    (str path "/index.html")]
    (doseq [dir ["assets"
                 "_site"
                 "templates"]]
      (fs/copy-dir (clojure.java.io/resource dir) temp-dir))


    (doseq [file ["Gemfile"
                  "Gemfile.lock"]]
      (fs/copy (clojure.java.io/resource file) (str path "/" file)))

    (fs/copy-dir (clojure.java.io/resource "src") (io/file (str path "/cljs/src" )))
    (fs/copy (clojure.java.io/resource "template.project.clj") (str path "/cljs/project.clj"))

    (spit (str path "/assets/data/data.js") (str "var renderData = "(json/generate-string data) ";"))
    path
    ;; (browse-url (str "file://" index))
    ))

(comment
  (prepare-tmp-dir
   [(cfg/make-chart-config
     {:id          "line"
      :x           "year"
      :y           "income"
      :x-order     "year"
      :series-type "line"
      :data        test-data
      })]))
