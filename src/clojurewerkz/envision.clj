(ns clojurewerkz.envision
  (:require [me.raynes.fs :as fs]))

(defn prepare-tmp-dir
  [data]
  (let [dir   (fs/temp-dir "envision-")
        path  (.getPath dir)
        index (str path "/index.html")]
    (fs/copy-dir (clojure.java.io/resource "public") dir)
    (spit index (render data))
    (browse-url (str "file://" index))))
