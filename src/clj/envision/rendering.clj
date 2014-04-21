(ns envision.rendering
  (:require [clojurewerkz.gizmo.widget :as widget]
            [clojurewerkz.gizmo.request :as request]
            [clojure.java.browse :refer [browse-url]]

            [me.raynes.fs :as fs]
            [cheshire.core :as json]


            [clojure-csv.core :as csv]
            [clojurewerkz.statistiker.distribution :as d]
            [clojurewerkz.statistiker.histograms :as hist]))

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
  "Renders a histogram of the vector"
  [v & {:keys [graph-title width height]}]
  (prepare-tmp-dir
   (map (fn [[k v]]
          {:x (format "%.2f" k) :y v})
        (hist/numerical-histogram 10 v))))

(defn render-scatterplot
  "Renders a histogram of the vector"
  [data & {:keys [x-fn y-fn metadata] :or {x-fn identity
                                           y-fn identity
                                           metatata {}}}]
  (prepare-tmp-dir
   (map (fn [i]
          {:x (x-fn i) :y (y-fn i)}))))
;; (take 2000 (d/normal-distribution 200 20))

;; Line Chart
;; Axes: identify axe type
;; Area
;; Scatterplot
;; Bar chart

(defn data
  []
  (let [[head & data] (csv/parse-csv (slurp "/Users/ifesdjeen/p/codecentric/envision/example_data.tsv") :delimiter \tab)
        cleanup (fn [i] (map (fn [n]
                              (try (Double/parseDouble n)
                                   (catch Exception _
                                     n)))
                            i))]
    (map #(zipmap head (cleanup %)) data)))

(defn render-free-graph
  "Renders a histogram of the vector"
  [data & {:keys [x-fn y-fn metadata] :or {x-fn identity
                                           y-fn identity
                                           metatata {}}}]
  (prepare-tmp-dir data))
