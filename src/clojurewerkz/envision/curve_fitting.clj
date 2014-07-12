(ns clojurewerkz.envision.curve-fitting
  (:require [clojurewerkz.envision.core            :as envision]
            [clojurewerkz.envision.chart-config    :as cfg]
            [clojurewerkz.envision.util            :refer [deep-merge add-serial-ids]]

            [clojurewerkz.statistiker.fitting      :as fitting]
            [clojurewerkz.statistiker.functions    :as fns]
            [clj.clojurewerkz.statistiker.datasets :as dss]))


(defn fit-curve
  [v x y fitter function]
  (envision/render
   [(cfg/make-chart-config
     {:id            "line"
      :headline      "Curve fitting (Gaussian)"
      :x             (name x)
      :y             (name y)
      :x-type        :measure
      :y-type        :measure
      :x-config      {:override-min (reduce min (map x v))}
      :series-type   "line"
      :data          (add-serial-ids
                      (concat
                       (map
                        #(assoc % :type "fitted")
                        (fitting/fit v
                                     x y
                                     fitter
                                     function
                                     30))
                       (map
                        #(assoc % :type "original")
                        v)))
      :series        ["serial-id" "type"]
      :interpolation :cardinal
      })])
  )

(comment
  (fit-curve dss/bell-curve-formed :a :b fitting/gaussian-fitter fns/gaussian-function)
  (fit-curve dss/poly-curve-formed
             :a
             :b
             #(fitting/polynomial-fitter % 100 [0 0 0 0 0])
             fns/polynomial-function))
