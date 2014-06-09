(ns clojurewerkz.envisioncljs.dimple
  (:require-macros [schema.macros :as sm])
  (:require [schema.core             :as s]))

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

(def line (-> js/dimple .-plot .-line))

;;
;; Configuration
;;

(defn configure-axis
  [axis {:keys [order-rule tick-format]}]
  (when tick-format
    (set! (.-tickFormat axis) tick-format))

  (when order-rule
    (.addOrderRule axis order-rule)))

(defn add-category-axis
  [chart axis-name field-name & {:keys [] :as axis-config}]
  (configure-axis
   (.addCategoryAxis chart axis-name field-name)
   axis-config)
  chart)

(defn add-measure-axis
  [chart axis-name field-name]
  (.addMeasureAxis chart axis-name field-name)
  chart)

(defrecord AxisTypeConstructor
    [^{:s s/Any} category
     ^{:s s/Any} measure])

(def axis-type-constructors
  (->AxisTypeConstructor add-category-axis
                         add-measure-axis))

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
  [chart series chart-type & {:keys [interpolation]}]
  (let [series (.addSeries chart series chart-type)]
    (when interpolation
      (set! (.-interpolation series) interpolation)))
  chart)

(defn draw
  [chart]
  (.draw chart)
  chart)

(defn filter-data
  [data field accepted-values]
  (.filterData js/dimple data field (clj->js accepted-values)))
