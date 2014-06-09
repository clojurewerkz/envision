(ns clojurewerkz.envisioncljs.line_chart
  (:require-macros [schema.macros :as sm])
  (:require [reagent.core            :as reagent :refer [atom]]
            [clojure.set             :as set]
            [schema.core             :as s]

            [clojurewerkz.envisioncljs.dimple :as dimple]
            [clojurewerkz.envisioncljs.utils  :as u]))

(enable-console-print!)

(sm/defrecord LineChartConfig
    [^{:s s/Str} x
     ^{:s s/Str} y
     ])

(sm/defrecord LineChartState
    [^{:s s/Any}                 chart
     ^{:s s/Bool}                did-unmount])

(defn validate-line-chart-state
  [a]
  (s/validate LineChartState a))

(defn make-empty-line-chart-state
  []
  (LineChartState. nil false))

(defn- init-line-chart
  [this line-chart-config line-chart-state data]
  (let [width   620
        height  350
        bound-x 60
        bound-y 30

        chart   (dimple/make-chart (u/dom-node this) width height)]

    (validate-line-chart-state
     (swap! line-chart-state #(assoc %
                                :chart  chart)))

    (-> chart
        (dimple/set-data     data)
        (dimple/add-axis     :category "x" (sm/safe-get line-chart-config :x) :order-rule "Date")
        (dimple/add-axis     :measure "y" (sm/safe-get line-chart-config :y))
        (dimple/add-series   nil dimple/line :interpolation "cardinal")
        (dimple/set-bounds   bound-x bound-y (- width (* 2 bound-x)) (- height (* 3 bound-y)))
        (dimple/draw))
    ))

(defn line-chart
  [line-chart-config line-chart-state data]
  (with-meta (fn []
               (let [a @line-chart-state]
                 [:div {:class "envision-chart"
                        :key   "envision-line-chart"} ""]))
    {:component-did-mount (fn [this]
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
