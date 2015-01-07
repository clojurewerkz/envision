(ns clojurewerkz.envisioncljs.continuum.main
  (:require-macros [schema.macros :as sm])
  (:require [reagent.core            :as reagent :refer [atom]]
            [clojure.set             :as set]
            [schema.core             :as s]

            [clojurewerkz.envisioncljs.button       :as b]
            [clojurewerkz.envisioncljs.chart_config :as cfg]
            [clojurewerkz.envisioncljs.dimple       :as dimple]
            [clojurewerkz.envisioncljs.utils        :as u]
            [clojurewerkz.envisioncljs.chart        :as chart]))

(enable-console-print!)

(defn test-graph-config
  [type]
  {:x             "x"
   :y             "y"
   :x-config      {:order-rule "time"
                   :tick-format "%H:%M"}
   :y-config      {:tick-format (if (= type "system.mem")
                                  "s" "s")}
   :y-type        :measure
   :x-type        :time
   :series-type   "line"
   :series        :group
   :interpolation :cardinal
   })

(defn dynamic-chart
  [metric-type transposer]
  (let [chart-data (atom [])]
    (.ajax js/jQuery (str "http://localhost:3000/dbs/" metric-type "/range")
           (clj->js {:success (fn [data]
                                (reset! chart-data (transposer
                                                    (js->clj data :keywordize-keys true))))}))
    [(chart/chart
      (cfg/make-chart-config (assoc (test-graph-config metric-type)
                               :id metric-type
                               :headline metric-type))
      (atom (chart/make-empty-chart-state))
      chart-data)]

    ))

(defn transpose-data
  [values]
  (fn [data]
    (->> values
         (map (fn [value]
                (map
                 (fn [datapoint]
                   {:x     (get datapoint :timestamp)
                    :y     (get datapoint value)
                    :group value})
                 data)))
         flatten)))

(def configuration-data
  {"system.tcp" {"in_segs" "DbtLong","retrans_segs" "DbtLong","out_segs" "DbtLong","passive_opens" "DbtLong","attempt_fails" "DbtLong","active_opens" "DbtLong","out_rsts" "DbtLong","curr_estab" "DbtLong","host" "DbtString","estab_resets" "DbtLong","in_errs" "DbtLong"},"system.mem.percent" {"free" "DbtLong","used" "DbtLong","actual_free" "DbtLong","total" "DbtLong","host" "DbtString","actual_used" "DbtLong","ram" "DbtLong"},"system.mem" {"free" "DbtLong","used" "DbtLong","actual_free" "DbtLong","total" "DbtLong","host" "DbtString","actual_used" "DbtLong","ram" "DbtLong"},"system.net" {"tcp_inbound_total" "DbtLong","all_inbound_total" "DbtLong","tcp_outbound_total" "DbtLong","all_outbound_total" "DbtLong","host" "DbtString"},"system.cpu" {"idle" "DbtDouble","stolen" "DbtDouble","irq" "DbtDouble","wait" "DbtDouble","soft_irq" "DbtDouble","sys" "DbtDouble","user" "DbtDouble","combined" "DbtDouble","host" "DbtString","nice" "DbtDouble"}})

(def numerical-types #{"DbtDouble" "DbtLong"})

(def numerical-type? #(get numerical-types %))

(defn chart-app
  []
  (let [wrapper-state-atom (atom {:selected-collection (first (keys configuration-data))})]
    (fn []
      (let [wrapper-state @wrapper-state-atom]
        [:div {:key "wrapper"}
         [:select {:key      "collection-select-box"
                   :onChange #(swap! wrapper-state-atom
                                     update-in [:selected-collection]
                                     (constantly (-> % (.-target) js/jQuery (.val))))}
          [:option {:key "empty"} "-"] ;; TODO: key here
          (for [coll (keys configuration-data)]
            [:option {:key coll} coll])]
         (if-let [coll (:selected-collection wrapper-state)]
           [:div {:key "wrapper-config"}
            [:h3 {:key "wrapper-header"} coll]
            [:table {:key (str "y-columns-" coll)
                     :class "table striped"}
             (for [[column type] (->> (get configuration-data coll)
                                      (filter #(numerical-type? (second %))  ))]
               [:tr
                [:td column]
                [:td type]
                ])
             ]
            ]
           [:div {:key "wrapper-config"}
            "empty"])



         ])

      ;; (dynamic-chart "system.mem"
      ;;                (transpose-data [:free :used :actual_free :total :actual_used :ram]))
      ;; (dynamic-chart "system.cpu"
      ;;                (transpose-data [:idle :stolen :irq :wait :soft_irq :sys :user :combined :nice]))
      ;; (dynamic-chart "system.net"
      ;;                (transpose-data []))
      ;; (dynamic-chart "system.tcp"
      ;;                (transpose-data []))

      )))



(reagent/render-component [chart-app] (.getElementById js/document "app"))
