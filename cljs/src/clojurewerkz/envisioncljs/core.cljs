(ns clojurewerkz.envisioncljs.core
  (:require-macros [schema.macros :as sm])
  (:require [reagent.core                           :as reagent :refer [atom]]
            [clojure.set                            :as set]
            [schema.core                            :as s]

            [clojurewerkz.envisioncljs.chart        :as c]
            [clojurewerkz.envisioncljs.button       :as b]
            [clojurewerkz.envisioncljs.chart_config :as cfg]
            [clojurewerkz.envisioncljs.dimple       :as dimple]
            [clojurewerkz.envisioncljs.utils        :as u]))


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
               [table (atom (:data config))]]
              [(c/chart
                (cfg/make-chart-config config)
                (atom (c/make-empty-chart-state))
                (atom (:data config))
                )]))])])))

(reagent/render-component [chart-app] (.getElementById js/document "app"))
