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
     ^{:s s/Bool}                did-unmount
     ^{:s s/Bool}                config-shown
     ])

(defn validate-chart-state
  [a]
  (s/validate ChartState a))

(defn make-empty-chart-state
  []
  (ChartState. nil false false))

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

(defn axis-measure-picker
  [axe chart-config chart-state]
  [:div
   [:h3 (str (clojure.string/capitalize axe) " axis measure")]
   (let [buttons (->> (sm/safe-get chart-config :data)
                      (first)
                      keys
                      (map name))]
     [b/button-list-widget
      (str (sm/safe-get chart-config :id) "-select")
      buttons
      :initial-state (sm/safe-get chart-config (keyword axe))
      :allow-empty   false
      :onChange      #(let [chart (sm/safe-get @chart-state :chart)]
                        ;; Add switch back to empty
                        (-> chart
                            (dimple/set-axis-measure axe %)
                            (dimple/draw)))])])

(defn parse-int
  [i]
  (js/parseInt i))

(defn cluster-filter
  [chart-config chart-state]
  (let [vals (->> (sm/safe-get chart-config :data)
                  (map :cluster-id)
                  distinct
                  (map str))]
    [:div
     [:h3 "Cluster Filter"]
     [b/button-list-widget
      (str (sm/safe-get chart-config :id) "-cluster-filter")
      vals
      :multi?        true
      :inline?       false
      :allow-empty   false
      :initial-state (set vals)
      :onChange      #(let [chart (sm/safe-get @chart-state :chart)]
                        (-> chart
                            (dimple/set-data (dimple/filter-data (clj->js (sm/safe-get chart-config :data))
                                                                 "cluster-id"
                                                                 %))
                            (dimple/draw)))]]))

(defn chart
  [chart-config chart-state]
  (with-meta (fn []
               (let [a  @chart-state
                     id (sm/safe-get chart-config :id)]
                 [:div {:class "highlight col-md-6 envision-chart"
                        :key   id}
                  [:h1 (sm/safe-get chart-config :headline)]
                  [b/button-list-widget
                   (str id "-config-toggle")
                   ["Toggle Config"]
                   :onChange #(swap! chart-state assoc :config-shown (not (nil? %)))]
                  (if (sm/safe-get a :config-shown)
                    [:table.table.top-aligned {:key (str id "-config")}
                     [:tr
                      [:td
                       (axis-measure-picker "x" chart-config chart-state)]
                      [:td
                       (axis-measure-picker "y" chart-config chart-state)]
                      [:td
                       (cluster-filter chart-config chart-state)]]]
                    [:div.dummie {:key (str id "-config")}])

                  ]))
    {:component-did-mount (fn [this]
                            (init-chart this
                                        chart-config
                                        chart-state
                                        ))}))


(defn table
  [data-atom]
  (let [sort-direction (atom nil)
        filters-atom (atom {})]
    (fn []
      (let [rows    @data-atom
            filters @filters-atom]
        [:div.table-responsive
         [:table.table.table-striped.table-condensed
          [:thead
           [:tr
            (for [key (-> rows first keys)]
              [:td {:onClick (fn []
                               (swap! sort-direction (fn [a]
                                                       (if (= key (first a))
                                                         [key (not (last a))]
                                                         [key true])))
                               (swap! data-atom (fn [old]
                                                  (let [[k d] @sort-direction]
                                                    (sort-by k (if d > <) old))
                                                  )))}
               [:div
                (name key)
                [:input {:type text :onChange (fn [e]
                                                (let [v (.-value (.-target e))]
                                                  (if (empty? v)
                                                    (swap! filters-atom dissoc key)
                                                    (swap! filters-atom assoc key v)
                                                    ))
                                                )}]
                ]
               ])]]

          [:tbody
           (for [row (filter (fn [r]
                               (every?
                                (fn [[fk fv]]
                                  (not (= -1 (.indexOf (str (get r fk)) fv))))
                                filters))
                             rows)]
             [:tr
              (for [[_ v] row]
                [:td (str v)])])]]]))))

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
               [table (atom (:data config))]]]
            [(chart
              (cfg/make-chart-config config)
              (atom (make-empty-chart-state))
              )]

            ))])])))

(reagent/render-component [chart-app] (.getElementById js/document "app"))
