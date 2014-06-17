(ns clojurewerkz.envision.chart-config
  (:require [schema.core                        :as s]))

(s/defrecord ChartConfig
    [^{:s s/Str}  id

     ^{:s s/Int}     width
     ^{:s s/Int}     height
     ^{:s s/Int}     top-x
     ^{:s s/Int}     top-y
     ^{:s s/Int}     chart-width
     ^{:s s/Int}     chart-height

     ^{:s s/Str}     x
     ^{:s s/Str}     x-type
     ^{:s s/Str}     y
     ^{:s s/Str}     y-type
     ^{:s s/Str}     z

     ^{:s s/Any}     x-config
     ^{:s s/Any}     y-config

     ^{:s s/Keyword} series-type
     ^{:s s/Any}     series ;; actually it's either string or vector of strings?
     ^{:s s/Keyword} interpolation

     ^{:s s/Any}     data
     ^{:s s/Any}     additional-series
     ^{:s s/Str}     headline
     ])

(defn make-chart-config
  [{:keys [id

           width
           height
           top-x
           top-y
           chart-width
           chart-height

           x
           x-type
           y
           y-type
           z

           x-config
           y-config

           series-type
           series
           interpolation

           data
           additional-series
           headline]}]

  (->ChartConfig id

                 width
                 height
                 top-x
                 top-y
                 chart-width
                 chart-height

                 x
                 x-type
                 y
                 y-type
                 z

                 x-config
                 y-config

                 series-type
                 series
                 interpolation

                 data
                 additional-series
                 headline))
