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
  [this chart-config chart-state-atom chart-data-atom]
  (let [chart (dimple/make-chart (u/dom-node this)
                                 (sm/safe-get chart-config :width)
                                 (sm/safe-get chart-config :height)
                                 )]

    (validate-chart-state
     (swap! chart-state-atom #(assoc % :chart chart)))

    (-> chart
        (dimple/set-data     (clj->js @chart-data-atom))
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

(defn- redraw
  [chart-config chart-state-atom chart-data-atom]
  (let [st       @chart-state-atom
        chart    (sm/safe-get st :chart)]
    (-> chart
        (dimple/set-data (clj->js @chart-data-atom))
        (dimple/draw))))

(defn chart
  [chart-config chart-state-atom chart-data-atom]
  (with-meta (fn []
               (let [chart-state @chart-state-atom
                     chart-data  @chart-data-atom
                     id          (sm/safe-get chart-config :id)]
                 [:div {:class "highlight col-md-6 envision-chart"
                        :id   id
                        :key   id}
                  [:h1 (sm/safe-get chart-config :headline)]
                  ]))
    {:component-did-mount (fn [this]
                            (init-chart this
                                        chart-config
                                        chart-state-atom
                                        chart-data-atom
                                        ))
     :component-did-update (fn [_ _ _]
                             (redraw chart-config
                                     chart-state-atom
                                     chart-data-atom))
     }))



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
                [:input {:type "text" :onChange (fn [e]
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


