(ns envision.hello
  (:require [reagent.core :as reagent :refer [atom]]
            [clojure.set :as set]
            [envision.utils :as utils]
            [envision.mini-histogram :as hist]))

(def data (atom (js->clj js/renderData)))

(def app-state (atom {:x "Month" :y "Unit Sales" :groups #{}}))
(def chart-state (atom {}))

(defn println
  [& all]
  (.log js/console (clj->js all)))

(defn select-control
  [options k]
  [:select
   {:class     "form-control"
    :value     (k @app-state)
    :on-change (fn [e]
                 (swap! app-state (fn [s]
                                    (let [st          @chart-state
                                          new-measure (-> e .-target .-value)
                                          axis        (get st (keyword (str (name k) "-axis")))]
                                      (set! (.-measure axis) new-measure)
                                      (.draw (get @chart-state :chart))
                                      (assoc s k new-measure)))))}
   (map (fn [option]
          [:option
           option])
        options)])

(defn groups
  []
  (let [st @app-state]
    [:div
     [:table {:class "table"}
      [:tbody
       (map (fn [i]
              [:tr
               [:td
                [:button {:type "button"
                          :on-click (fn [e]
                                      (let [val (-> e .-target .-value)]
                                        (if (nil? (get-in st [:groups val]))
                                          (swap! app-state (fn [st]
                                                             (assoc st
                                                               :groups
                                                               (conj (:groups st) val))))
                                          (swap! app-state (fn [st]
                                                             (assoc st
                                                               :groups
                                                               (disj (:groups st) val)))))))
                          :class (str "btn btn-"
                                      (if (not (nil? (get (get st :groups) i)))
                                        "success"
                                        "default"))
                          :value i}
                 i]]
               [:td [hist/wrapped-mini-histogram i data]]])
            (-> data deref first keys))]]]))

(defn chart
  []
  [:div])

(def wrapped-chart
  (with-meta chart
    {:should-component-update (fn [this] true)
     :component-did-mount (fn [this]
                            (let [st     @app-state
                                  width  500
                                  height 320
                                  svg    (.newSvg js/dimple (utils/dom-node this) width height)
                                  chart  (new dimple/chart svg (clj->js @data))

                                  x-axis (.addCategoryAxis chart "x" (get-in st [:x]))
                                  y-axis (.addMeasureAxis chart "y" (get-in st [:y]))]

                              (swap! chart-state (fn [s]
                                                   (assoc s
                                                     :x-axis x-axis
                                                     :y-axis y-axis
                                                     :chart  chart)))

                              (.setBounds chart 50 50 (- width 50) (- height 120))
                              (.addSeries chart (clj->js (:groups st)) (-> js/dimple .-plot .-bubble))
                              (.draw chart)))}))

(defn todo-app [props]
  (fn []
    [:form.form-horizontal
     [:div.col-md-7
      [:div.form-group
       [:label "X"]
       [select-control (keys (first @data)) :x]]
      [:div.form-group
       [:label "Y"]
       [select-control (keys (first @data)) :y]]
      [:div.form-group
       [:label "Groups"]
       [groups]]]
     [:div.col-md-5
      (let [st @app-state]
        [wrapped-chart {:key (rand-int 1000)}])]]))


(reagent/render-component [todo-app] (.getElementById js/document "app"))
