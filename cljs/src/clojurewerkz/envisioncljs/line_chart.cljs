(ns clojurewerkz.envisioncljs.line_chart
  (:require-macros [schema.macros :as sm])
  (:require [reagent.core            :as reagent :refer [atom]]
            [clojure.set             :as set]
            [schema.core             :as s]))

(enable-console-print!)

(sm/defrecord LineChartConfig
    [^{:s s/Str} x
     ^{:s s/Str} y

     ])
(sm/defrecord LineChartState
    [^{:s s/Any}                 chart
     ^{:s s/Any}                 x-axis
     ^{:s s/Any}                 y-axis
     ^{:s s/Any}                 data-raw
     ^{:s s/Bool}                did-unmount])

(defn validate-line-chart-state
  [a]
  (s/validate LineChartState a))

(defn make-empty-line-chart-state
  []
  (LineChartState. nil nil nil nil false))

(defn- init-line-chart
  [this line-chart-config line-chart-state data]
  (let [width             520
        height            250

        svg               (.newSvg js/dimple (.getDOMNode this) width height)
        chart             (new dimple/chart svg data)

        x-axis            (.addCategoryAxis  chart "x" (sm/safe-get line-chart-config :x))
        y-axis            (.addMeasureAxis chart "y" (sm/safe-get line-chart-config :y))]

    ;;(set! (.-tickFormat x-axis ) "%H:%M:%S")
    ;;(set! (.-tickFormat y-axis ) "s")

    (validate-line-chart-state
     (swap! line-chart-state #(assoc %
                                :x-axis x-axis
                                :y-axis y-axis
                                :chart  chart)))

    (.setBounds chart 40 30 (- width 50) (- height 50))
    (.addSeries chart nil (-> js/dimple .-plot .-line))
    (.draw chart)
    ))

(defn line-chart
  [line-chart-config line-chart-state data]
  (with-meta (fn []
               (let [a @line-chart-state]
                 [:div {:class "envision-chart"
                        :key   "envision-line-chart"} ""]))
    {:should-component-update (fn [this]
                                true)
     :component-did-mount
     (fn [this]
       (init-line-chart this
                        line-chart-config
                        line-chart-state
                        data))}
    ))

(defn line-chart-app
  []
  (fn []
    [:div
     [(line-chart
       (->LineChartConfig "Month" "Unit Sales")
       (atom (make-empty-line-chart-state))
       js/renderData)]]))

(reagent/render-component [line-chart-app] (.getElementById js/document "app"))
