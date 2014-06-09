(ns clojurewerkz.envision
  (:require [me.raynes.fs        :as fs]
            [cheshire.core       :as json]
            [clojure.java.io     :as io]

            [clojure.java.browse :refer [browse-url]]))

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
    (fs/copy (clojure.java.io/resource "project.clj") (str path "/cljs/project.clj"))

    (spit (str path "/assets/data/data.json") (json/generate-string data))
    path
    ;; (browse-url (str "file://" index))
    ))
