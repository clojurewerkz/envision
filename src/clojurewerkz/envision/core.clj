(ns clojurewerkz.envision.core
  (:require [me.raynes.fs                       :as fs]
            [cheshire.core                      :as json]
            [clojure.java.io                    :as io]
            [schema.core                        :as s]
            [clojurewerkz.envision.chart-config :as cfg]

            [clojurewerkz.statistiker.histograms :as hist]
            [clojurewerkz.statistiker.regression :as regression]
            [clojure.java.browse :refer [browse-url]]))


(defn prepare-tmp-dir
  "Prepares a tmp directory with all templates and returns a path to it"
  [data]
  (let [temp-dir (fs/temp-dir "envision-")
        path     (.getPath temp-dir)
        index    (str path "/index.html")]
    (doseq [dir ["assets"
                 "templates"]]
      (fs/copy-dir (clojure.java.io/resource dir) temp-dir))

    (fs/copy-dir (clojure.java.io/resource "src")
                 (io/file (str path "/cljs/src" )))

    (fs/copy (clojure.java.io/resource "template.project.clj")
             (str path "/cljs/project.clj"))

    (spit (str path "/assets/data/data.js")
          (str "var renderData = "(json/generate-string data) ";"))
    path
    ;; (browse-url (str "file://" index))
    ))

(defn histogram
  "Histogram accepts a vector of values"
  [bins data]
  (let [hist (->> (hist/empirical-distribution bins data)
                  (map (fn [[x y]] {:x x :y y})))]
    (prepare-tmp-dir
     [(cfg/make-chart-config
       {:id            "histogram"
        :x             "x"
        :y             "y"
        :series-type   "bar"
        :data          hist})])))

(defn linear-regression
  [data x-field y-field series]
  (let [d             (map x-field data)
        [min-x max-x] [(apply min d) (apply max d)]]
    (prepare-tmp-dir
     [(cfg/make-chart-config
       {:id                "bubble"
        :x                 x-field
        :x-type            :measure
        :y                 y-field
        :y-type            :measure
        :x-config          {:order-rule "year"
                            :override-min min-x}
        :series-type       "bubble"
        :data              data
        :series            series
        :additional-series [:linear-trend {:data (let [{:keys [intercept slope]} (regression/linear-regression
                                                                                  data x-field y-field)]

                                                   [{:cx min-x :cy (+ intercept (* slope min-x))},
                                                    {:cx max-x :cy (+ intercept (* slope max-x))}])}]
        })
      ])))


(defn a []
  (linear-regression
   (flatten (for [i (range 0 20)]
              [{:year (+ 2000 i)
                :income (+ 10 i (rand-int 10))
                :series "series-1"}
               {:year (+ 2000 i)
                :income (+ 10 i (rand-int 20))
                :series "series-2"}]
              ))
   :year
   :income
   [:year :income :series]))
