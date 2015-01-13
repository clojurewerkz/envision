(ns clojurewerkz.envisioncljs.continuum.main
  (:require-macros [schema.macros :as sm])
  (:require [reagent.core            :as reagent :refer [atom]]
            [clojure.set             :as set]
            [cljs.reader             :as reader]
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
    (.ajax js/jQuery
           ;;(str "http://localhost:3000/dbs/" metric-type "/range")
           "/dbs/system.mem/range?timeGroup=500000&aggregate=min&fields=free,used,actual_free,total,actual_used,ram"
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
         flatten

         )))

(defn transpose-group-data
  [values]
  (fn [data]
    (->> data
         (map (fn [[ts e]] (assoc e :timestamp (reader/read-string (name ts)))))
         ((transpose-data values))
         )
    ))

(def configuration-data
  {"system.tcp" {"in_segs" "DbtLong","retrans_segs" "DbtLong","out_segs" "DbtLong","passive_opens" "DbtLong","attempt_fails" "DbtLong","active_opens" "DbtLong","out_rsts" "DbtLong","curr_estab" "DbtLong","host" "DbtString","estab_resets" "DbtLong","in_errs" "DbtLong"},"system.mem.percent" {"free" "DbtLong","used" "DbtLong","actual_free" "DbtLong","total" "DbtLong","host" "DbtString","actual_used" "DbtLong","ram" "DbtLong"},"system.mem" {"free" "DbtLong","used" "DbtLong","actual_free" "DbtLong","total" "DbtLong","host" "DbtString","actual_used" "DbtLong","ram" "DbtLong"},"system.net" {"tcp_inbound_total" "DbtLong","all_inbound_total" "DbtLong","tcp_outbound_total" "DbtLong","all_outbound_total" "DbtLong","host" "DbtString"},"system.cpu" {"idle" "DbtDouble","stolen" "DbtDouble","irq" "DbtDouble","wait" "DbtDouble","soft_irq" "DbtDouble","sys" "DbtDouble","user" "DbtDouble","combined" "DbtDouble","host" "DbtString","nice" "DbtDouble"}})

(def numerical-types #{"DbtDouble" "DbtLong"})

(def numerical-type? #(get numerical-types %))

(defn toggle
  [coll item]
  (if (get coll item)
    (disj coll item)
    (conj coll item)))

(defn chart-app
  []
  (let [wrapper-state-atom (atom {:selected-collection  "system.mem"
                                  :selected-fields      (->> (get configuration-data "system.mem")
                                                             (filter #(numerical-type? (second %)))
                                                             keys
                                                             set)
                                  :selected-time-period 0
                                  :selected-aggregate   nil
                                  })]
    (fn []
      (let [{:keys [selected-collection
                    selected-fields
                    selected-time-period
                    selected-aggregate]}
            @wrapper-state-atom]
        [:div.row
         [:div.col-md-6 {:key "wrapper"}
          [:div
           [:h3 "Time Period"]
           [:div.btn-group
            (for [[name time-period] {"None"  0
                                      "1 min" 1000
                                      "5 min" 5000
                                      "10 min" 10000}]
              [:button.btn {:onClick #(swap! wrapper-state-atom
                                             update-in [:selected-time-period]
                                             (constantly time-period))
                            :class (if (= time-period selected-time-period)
                                     "btn-active"
                                     "btn-default")}
               name]
              )]]
          (if selected-collection
            [:div {:key "wrapper-config"}
             [:h3 "Collection"]
             [:select {:key      "collection-select-box"
                       :value    selected-collection
                       :onChange #(do
                                    (swap! wrapper-state-atom
                                           update-in [:selected-fields]
                                           (constantly []))
                                    (swap! wrapper-state-atom
                                           update-in [:selected-collection]
                                           (constantly (-> % (.-target) js/jQuery (.val)))))}
              [:option {:key "empty"} "-"] ;; TODO: key here
              (for [coll (keys configuration-data)]
                [:option {:key coll} coll])]
             [:br][:br]
             [:table {:key (str "y-columns-" selected-collection)
                      :class "table striped"}
              (for [[column type] (->> (get configuration-data selected-collection)
                                       (filter #(numerical-type? (second %))  ))]
                [:tr {:onClick #(swap! wrapper-state-atom
                                       update-in [:selected-fields]
                                       (fn [a] (toggle a column)))
                      :class   (if (not (nil? (get selected-fields column)))
                                 "active"
                                 ""
                                 )}
                 [:td column]
                 [:td type]
                 ])
              ]]
            [:div {:key "wrapper-config"}
             "empty"])
          [:div
           [:h3 "Aggregate"]
           [:div.btn-group
            (for [[name aggregate] {"None"  nil
                                    "Avg" "Avg"
                                    "Min" "Min"
                                    "Max" "Max"}]
              [:button.btn {:onClick #(swap! wrapper-state-atom
                                             update-in [:selected-aggregate]
                                             (constantly aggregate))
                            :class (if (= aggregate selected-aggregate)
                                     "btn-active"
                                     "btn-default")}
               name]
              )]]
          ]

         (dynamic-chart selected-collection
                        (transpose-group-data (map keyword selected-fields)))
         ]
        )

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
