(ns envision.rendering
  (:require [clojurewerkz.gizmo.widget :as widget]
            [clojurewerkz.gizmo.request :as request]
            [clojure.java.browse :refer [browse-url]]

            [me.raynes.fs :as fs]
            [cheshire.core :as json]

            [clojurewerkz.statistiker.distribution :as d]
            [clojurewerkz.statistiker.histograms :as hist]))


;; (clojure.java.io/resource "public/javascripts/application.js")
(defn render
  [data]
  (let [env {:widgets {:main-content 'envision.widgets.home/index-content,
                       :includes {:attrs {:src "public/javascripts/lib/build/application.js" :type "text/javascript"},
                                  :tag :script}
                       :data {:attrs {:type "application/javascript"},
                              :content (str "var renderData=" (json/generate-string data) ";")
                              :tag   :script}
                       }}]

    (request/with-request
      env
      (widget/with-trace
        (-> ((last (first (widget/all-layouts))))
            (widget/inject-core-widgets (:widgets env))
            (widget/interpolate-widgets env)
            widget/render*)))))

(defn prepare-tmp-dir
  [data]
  (let [dir   (fs/temp-dir "envision-")
        path  (.getPath dir)
        index (str path "/index.html")]
    (fs/copy-dir (clojure.java.io/resource "public") dir)
    (spit index (render data))
    (browse-url (str "file://" index))))

(defn render-histogram
  [v & {:keys [graph-title]}]
  (prepare-tmp-dir
   (map (fn [[k v]]
          {:x (format "%.2f" k) :y v})
        (hist/numerical-histogram 10 v))))

;; (take 2000 (d/normal-distribution 200 20))
