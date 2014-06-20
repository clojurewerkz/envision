(ns clojurewerkz.envisioncljs.chart
  (:require-macros [schema.macros :as sm])
  (:require [reagent.core            :as reagent :refer [atom]]
            [clojure.set             :as set]
            [schema.core             :as s]


            [clojurewerkz.envisioncljs.button       :as b]
            [clojurewerkz.envisioncljs.chart_config :as cfg]
            [clojurewerkz.envisioncljs.dimple       :as dimple]
            [clojurewerkz.envisioncljs.utils        :as u]))

(enable-console-print!)

(sm/defrecord ChartState
    [^{:s s/Any}                 chart
     ^{:s s/Bool}                did-unmount])

(defn validate-chart-state
  [a]
  (s/validate ChartState a))

(defn make-empty-chart-state
  []
  (ChartState. nil false))

(defn- init-chart
  [this chart-config chart-state]
  (let [chart (dimple/make-chart (u/dom-node this)
                                 (sm/safe-get chart-config :width)
                                 (sm/safe-get chart-config :height)
                                 )]

    (validate-chart-state
     (swap! chart-state #(assoc %
                           :chart chart)))

    (-> chart
        (dimple/set-data     (clj->js
                              (->> (sm/safe-get chart-config :data)
                                   identity
                                   )))
        (dimple/add-axis     (sm/safe-get chart-config :x-type)
                             "x"
                             (sm/safe-get chart-config :x)
                             (sm/safe-get chart-config :x-config))

        (dimple/add-axis     (sm/safe-get chart-config :y-type)
                             "y"
                             (sm/safe-get chart-config :y)
                             (sm/safe-get chart-config :y-config))

        (dimple/add-series   (sm/safe-get chart-config :series)
                             (sm/safe-get chart-config :series-type)
                             {:interpolation (sm/safe-get chart-config :interpolation)})

        ((fn [chart]
           (when-let [additional-series (sm/safe-get chart-config :additional-series)]
             (doseq [[type config] (partition 2 additional-series)]
               (dimple/add-series chart
                                  (.-categoryFields (first (.-series chart)))
                                  (keyword type)
                                  config)
               ))
           chart))
        (dimple/set-bounds   (sm/safe-get chart-config :top-x)
                             (sm/safe-get chart-config :top-y)
                             (sm/safe-get chart-config :chart-width)
                             (sm/safe-get chart-config :chart-height)
                             )
        (dimple/draw))))

(defn chart
  [chart-config chart-state]
  (with-meta (fn []
               (let [a @chart-state]
                 [:div {:class "col-md-6 envision-chart"
                        :key   (sm/safe-get chart-config :id)}
                  [:h1 (sm/safe-get chart-config :headline)]
                  (for [axe ["x" "y"]]
                    [b/button-list-widget
                     (str (sm/safe-get chart-config :id) "-select")
                     (->> (sm/safe-get chart-config :data)
                          (first)
                          keys
                          (map name))
                     :onChange #(let [chart (sm/safe-get @chart-state :chart)]
                                  (-> chart
                                      (dimple/set-axis-measure axe %)
                                      (dimple/draw)))])

                  ;; TODO: this requires massive refactoring
                  [b/button-list-widget
                   (str (sm/safe-get chart-config :id) "-cluster-filter")
                   (->> (sm/safe-get chart-config :data)
                        (map :cluster-id)
                        distinct)
                   :multi? true
                   :onChange #(let [chart (sm/safe-get @chart-state :chart)]
                                (-> chart
                                    (dimple/set-data (dimple/filter-data (clj->js (sm/safe-get chart-config :data))
                                                                         "cluster-id"
                                                                         %))
                                    (dimple/draw)))]
                  ]))
    {:component-did-mount (fn [this]
                            (init-chart this
                                        chart-config
                                        chart-state
                                        ))}))


(defn table
  [rows]
  [:div.table-responsive
   [:table.table.table-striped.table-condensed
    (for [row rows]
      [:tr
       (for [[_ v] row]
         [:td (str v)])])]])

(defn chart-app
  []
  (fn []
    (let [data (js->clj js/renderData :keywordize-keys true)]
      [:div
       (for [row (partition 2 2 nil data)]
         [:div.row
          (for [config row]
            (if (= "table" (:series-type config))
              [:div.col-md-6
               (table (:data config))]
              [(chart
                (cfg/make-chart-config config)
                (atom (make-empty-chart-state))
                )]

              ))])])))

(reagent/render-component [chart-app] (.getElementById js/document "app"))
