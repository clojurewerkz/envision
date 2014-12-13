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
  {:x             "timestamp"
   :y             "value"
   :x-config      {:order-rule "time"
                   :tick-format "%H:%M"}
   :y-config      {:tick-format (if (= type "system.mem")
                                  "s" "%")}
   :y-type        :measure
   :x-type        :time
   :series-type   "line"
   :series        :subtype
   :interpolation :cardinal
   })

(defn dynamic-chart
  [metric-type]
  (let [chart-data (atom [])]
    (.ajax js/jQuery (str "http://localhost:3000/dbs/" metric-type "/range")
           (clj->js {:success (fn [data]
                                (reset! chart-data (:Right (js->clj data :keywordize-keys true))))}))
    [(chart/chart
      (cfg/make-chart-config (assoc (test-graph-config metric-type)
                               :id metric-type
                               :headline metric-type))
       (atom (chart/make-empty-chart-state))
       chart-data)]

      ))
(defn chart-app
  []
  (fn []
    [:div {:key "wrapper"}
     (dynamic-chart "system.mem")
     (dynamic-chart "system.cpu")
     ]


    ))


(reagent/render-component [chart-app] (.getElementById js/document "app"))
