(ns clojurewerkz.envisioncljs.dimple
  (:require-macros [schema.macros :as sm])
  (:require [schema.core                            :as s]
            [clojurewerkz.envisioncljs.chart_config :as cfg]))

(sm/defrecord Interpolations
    [^{:s s/Str} linear
     ^{:s s/Str} linear-closed
     ^{:s s/Str} step-before
     ^{:s s/Str} step-after
     ^{:s s/Str} basis
     ^{:s s/Str} basis-open
     ^{:s s/Str} basis-closed
     ^{:s s/Str} bundle
     ^{:s s/Str} cardinal
     ^{:s s/Str} cardinal-open
     ^{:s s/Str} cardinal-closed
     ^{:s s/Str} monotone])

(def interpolations
  (->Interpolations
   "linear"
   "linear-closed"
   "step-before"
   "step-after"
   "basis"
   "basis-open"
   "basis-closed"
   "bundle"
   "cardinal"
   "cardinal-open"
   "cardinal-closed"
   "monotone"
   ))

(defn- make-svg
  [node width height]
  (.newSvg js/dimple node width height))

(def chart
  (.-chart js/dimple))

(defn make-chart
  [node width height]
  (let [svg         (make-svg node width height)
        constructor (.-chart js/dimple)
        chart       (new constructor svg nil)]
    chart))

;;
;;
;;

(sm/defrecord SeriesTypeConstructor
    [^{:s s/Any} line
     ^{:s s/Any} bubble
     ^{:s s/Any} area
     ^{:s s/Any} bar
     ^{:s s/Any} linear-trend])

(def line   (-> js/dimple .-plot .-line))
(def bubble (-> js/dimple .-plot .-bubble))
(def area   (-> js/dimple .-plot .-area))
(def bar    (-> js/dimple .-plot .-bar))


(def linear-trend (clj->js {:stacked       false
                            :grouped       false
                            :supportedAxes (clj->js ["x" "y"])

                            :draw          (fn [chart series duration]
                                             (let [line (-> js/d3
                                                            (.-svg)
                                                            (.line)
                                                            (.x (fn [d]
                                                                  (-> js/dimple
                                                                      .-_helpers
                                                                      (.cx d chart series))))
                                                            (.y (fn [d]
                                                                  (-> js/dimple
                                                                      .-_helpers
                                                                      (.cy d chart series)))))
                                                   path (or (.-path series)
                                                            (-> (.-svg chart)
                                                                (.append "g")
                                                                (.attr "class" "linear-trend")
                                                                (.append "path")
                                                                (.datum  (.-chartData series))
                                                                (.attr "d" line)
                                                                (.style "stroke" "blue")))]

                                               (-> path
                                                   (.attr "d" line))
                                               (set! (.-path series) path)))}))

;; (def linear-trend    (-> js/dimple .-plot .-linearTrend))

(def series-type-constructors
  (->SeriesTypeConstructor line
                           bubble
                           area
                           bar
                           linear-trend))
;;
;; Configuration
;;

(defn configure-axis
  [axis {:keys [] :as axis-config}]

  (let [fields {:category-fields    "categoryFields"
                :colors             "colors"
                :clamp              "clamp"
                :font-size          "fontSize"
                :font-family        "fontFamily"
                :gridline-shapes    "gridlineShapes"
                :hidden             "hidden"
                :log-base           "logBase"
                :use-log            "useLog"
                :measure            "measure"
                :override-min       "overrideMin"
                :show-gridlines     "showGridlines"
                :show-percent       "showPercent"
                :title-shape        "titleShape"
                :tick-format        "tickFormat"
                :time-field         "timeField"
                :title              "title"
                :floating-bar-width "floatingBarWidth"
                :date-parse-format  "dateParseFormat"
                :ticks              "ticks"
                :time-period        "timePeriod"
                :time-interval      "timeInterval"
                :order-rule         "orderRule"
                :group-order-rule   "groupOrderRule"}]

    (doseq [[k field] fields]
      (when-let [v (sm/safe-get axis-config k)]
        (aset axis field v))))

  (when-let [v (sm/safe-get axis-config :order-rule)]
    (.addOrderRule axis v))

  (when-let [v (sm/safe-get axis-config :group-order-rule)]
    (.addOrderRule axis v)))

(defn add-category-axis
  [chart axis-name field-name axis-config]
  (configure-axis
   (.addCategoryAxis chart axis-name field-name)
   axis-config)
  chart)

(defn add-measure-axis
  [chart axis-name field-name axis-config]
  (configure-axis
   (.addMeasureAxis chart axis-name field-name)
   axis-config)
  chart)

(defn add-time-axis
  [chart axis-name field-name axis-config]
  (configure-axis
   (.addTimeAxis chart axis-name field-name)
   axis-config)
  chart)

(defn add-pct-axis
  [chart axis-name field-name axis-config]
  (configure-axis
   (.addPctAxis chart axis-name field-name)
   axis-config)
  chart)

(defn add-log-axis
  [chart axis-name field-name axis-config]
  (configure-axis
   (.addLogAxis chart axis-name field-name)
   axis-config)
  chart)

(sm/defrecord AxisTypeConstructor
    [^{:s s/Any} category
     ^{:s s/Any} measure
     ^{:s s/Any} time
     ^{:s s/Any} log
     ^{:s s/Any} pct
     ])

(def axis-type-constructors
  (->AxisTypeConstructor add-category-axis
                         add-measure-axis
                         add-time-axis
                         add-log-axis
                         add-pct-axis
                         ))

(defn add-axis
  [chart axis-type & args]
  (let [c (sm/safe-get axis-type-constructors
                       axis-type)]
    (apply c chart args)))

(defn set-data
  [chart data]
  (set! (.-data chart) data)
  chart)

(defn set-bounds
  "Set the size of the plot within the svg. "
  [chart x y width height]
  (.setBounds chart x y width height)
  chart)

(defn add-series
  [chart series-literals series-type {:keys [interpolation data force-data y y-type y-config] :as conf}]
  (let [series (if (and y y-config y-type)
                 (do
                   (add-axis chart
                             (keyword y-type)
                             "y"
                             y
                             (cfg/make-axis-config y-config))
                   (.addSeries chart
                               (clj->js series-literals)
                               (sm/safe-get series-type-constructors
                                            series-type)
                               (clj->js [(first (.-axes chart))
                                         (last (.-axes chart))])))
                 (.addSeries chart
                             (clj->js series-literals)
                             (sm/safe-get series-type-constructors
                                          series-type)))]

    (when interpolation
      (set! (.-interpolation series) (sm/safe-get interpolations (keyword interpolation))))

    (when data
      (set! (.-chartData series) (clj->js data)))

    (when force-data
      (set! (.-data series) (clj->js force-data))))
  chart)

(defn draw
  [chart]
  (.draw chart)
  chart)

(defn filter-data
  [data field accepted-values]
  (.filterData js/dimple data field (clj->js accepted-values)))

(defn set-axis-measure
  [chart axis measure]
  (let [axe (->> chart
                 (.-axes)
                 (filter #(= axis (.-position %)))
                 first
                 )]
    (aset axe "measure" measure))
  chart)
