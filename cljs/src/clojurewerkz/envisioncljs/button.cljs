(ns clojurewerkz.envisioncljs.button
  (:require [reagent.core :as reagent :refer [atom]]))

(defn button-widget
  [text & {:keys [] :as config}]
  [:button.btn.btn-xs (assoc config :key (str "button-widget-" text))
   text])

(defn toggle
  [old new]
  (if (= old new) nil new))

(defn toggle-multiple
  [st new]
  (println (first st) new )
  (if (nil? (get st new))
    (conj st new)
    (disj st new)))

(defn maybe
  [allow-empty toggle-fn]
  (fn [old-v new-v]
    (let [new-v (toggle-fn old-v new-v)]
      (if (and (not allow-empty)
               (empty? new-v))
        old-v
        new-v))))

(defn is-active?
  [current active]
  (= current active))

(defn is-active-multiple?
  [current active]
  (not (nil? (get active current))))

(defn button-list-widget
  [key items & {:keys [onChange single-value inline? initial-state multi? active? allow-empty]
                :or {inline?     true
                     multi?      false
                     onChange    identity
                     allow-empty true}}]
  (let [initial-state (or initial-state (if multi? #{} nil))
        active?       (if multi? is-active-multiple? is-active?)
        active-state  (atom initial-state)
        allow-empty   allow-empty]
    (fn []
      (let [active @active-state]
        [:div.btn-group-vertical
         (for [item items]
           (button-widget item
                          :value item
                          :class (if (active? item active) "btn-success" "btn-default")
                          :onClick (fn [e]
                                     (let [val (.val (js/jQuery (.-target e)))]
                                       (onChange
                                        (swap! active-state (maybe allow-empty
                                                                   (if multi?
                                                                     toggle-multiple
                                                                     toggle))
                                               val))))))]))))

(defn propagate-state
  ([state-atom]
     #(reset! state-atom %))
  ([state-atom update-path]
     #(swap! state-atom assoc-in update-path %))
  ([state-atom update-path f]
     #(swap! state-atom assoc-in update-path (f %)))
  ([state-atom update-path f callback]
     #(do
        (swap! state-atom assoc-in update-path (f %))
        (callback))))

(comment
  (reagent/render-component [button-list-widget
                             :key
                             ["a" "b" "c" "d"]
                             :onChange (fn [a] (.log js/console a)) ;;
                             :multi? true
                             :inline? true]
                            (.getElementById js/document "main-content")))
