(ns envision.mini-histogram
  (:require [envision.utils :as utils]))

(defn domain
  [this v]
  (.domain this (clj->js v)))

(defn range
  [this v]
  (.range this (clj->js v)))

(defn linear
  [this]
  (.linear this))

(defn mini-histogram
  [field]
  [:div {:data-field field
         :class      "chart"}])

(defn mini-histogram
  [field data]
  [:div {:data-field field
         :class      "chart"}])

(defn normalise-negative
  [values]
  (let [m (apply min values)]
    (if (< m 0)
      (map #(+ (* -1 m) %) values)
      values)))

(defn categorical-histogram-layout
  [values]
  (let [m (apply max values)
        length (/ m (count values))]
    (clj->js
     (map (fn [v xx]
            {:y      v
             :length v
             :x      xx
             :dx     length})
          values
          (iterate #(+ % length) 0)))))

(defn numerical-histogram-layout
  [values]
  ((.histogram d3/layout) (clj->js values)))


(def wrapped-mini-histogram
  (with-meta mini-histogram
    {:component-did-mount (fn [this]
                            (let [data         (deref (last (.-cljsArgv (.-props this))))
                                  node         (utils/dom-node this)
                                  field        (utils/data-field node :data-field)

                                  width        300
                                  height       35

                                  values       (map #(get % field) data)

                                  categorical? (string? (first values))

                                  values       (if categorical?
                                                 (-> values frequencies vals)
                                                 (normalise-negative values))

                                  m            (apply max values)

                                  x            (-> d3/scale
                                                   linear
                                                   (domain [0 m])
                                                   (range  [0 width]))

                                  hist         ((if categorical?
                                                  categorical-histogram-layout
                                                  numerical-histogram-layout)
                                                values)

                                  y            (-> d3/scale
                                                   linear
                                                   (domain [0      (apply max (map #(.-y %) hist))])
                                                   (range  [height 0]))

                                  x-axis       (-> d3/svg .axis (.scale x) (.orient bottom))

                                  svg          (-> (d3/select node)
                                                   (.append "svg")
                                                   (.attr   "width" width)
                                                   (.attr   "height" height)
                                                   (.append "g")
                                                   (.attr   "transform" (str "translate(0,0)")))

                                  bar          (-> svg
                                                   (.selectAll ".bar")
                                                   (.data      hist)
                                                   (.enter)
                                                   (.append    "g")
                                                   (.attr      "class" "bar")
                                                   (.attr      "transform" (fn [d]
                                                                             (str "translate("
                                                                                  (x (.-x d))
                                                                                  ","
                                                                                  (y (.-y d))
                                                                                  ")"))))]

                              (-> bar
                                  (.append "rect")
                                  (.attr   "x" 1)
                                  (.attr   "width" (- (x (.-dx (nth hist 0))) 1))
                                  (.attr   "height" (fn [d] (y (- height (.-y d))))))))}))
