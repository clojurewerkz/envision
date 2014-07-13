(ns clojurewerkz.envision.curve-fitting
  (:require [clojurewerkz.envision.core            :as envision]
            [clojurewerkz.envision.chart-config    :as cfg]
            [clojurewerkz.envision.util            :refer [deep-merge add-serial-ids]]

            [clojurewerkz.statistiker.fitting      :as fitting]
            [clojurewerkz.statistiker.functions    :as fns]
            [clj.clojurewerkz.statistiker.datasets :as dss]))


(defn fit-curve
  ([v x y fitter function]
     (fit-curve v x y fitter function {}))
  ([v x y fitter function config-overrides]
     (cfg/make-chart-config
      (deep-merge
       {:id                "line"
        :x                 (name x)
        :y                 (name y)
        :x-type            :measure
        :y-type            :measure
        :x-config          {:override-min (reduce min (map x v))}
        :series-type       "line"
        :data              (->> (fitting/fit v
                                             x y
                                             fitter
                                             function
                                             30)
                                (map #(assoc % :type "fitted"))
                                add-serial-ids)

        :series            ["serial-id" "type"]
        :additional-series [:bubble {:force-data (->> v
                                                (map #(assoc % :type "original"))
                                                add-serial-ids)}]
        :interpolation     :cardinal}
       config-overrides))))

(comment
  (envision/render
   [(fit-curve dss/bell-curve-formed
               :a :b
               fitting/gaussian-fitter
               fns/gaussian-function
               {:headline "Gaussian Fitter"})
    (fit-curve dss/poly-curve-formed
               :a :b
               #(fitting/polynomial-fitter % 100 [0 0 0 0 0])
               fns/polynomial-function
               {:headline "Poly Curve Fitting"})
    ]))
