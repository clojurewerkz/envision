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
     ^{:s s/Str}     y
     ^{:s s/Str}     z

     ^{:s s/Str}     x-order
     ^{:s s/Keyword} series-type
     ^{:s s/Any}     series ;; actually it's either string or vector of strings?
     ^{:s s/Keyword} interpolation

     ^{:s s/Any} data])

(defn make-chart-config
  [{:keys [id

           width
           height
           top-x
           top-y
           chart-width
           chart-height

           x
           y
           z

           x-order
           series-type
           series
           interpolation

           data]
    :or {id            "chart"

         width         620
         height        350
         top-x         60
         top-y         30
         chart-width   500
         chart-height  250

         series-type   :line}}]

  (->ChartConfig id

                 width
                 height
                 top-x
                 top-y
                 chart-width
                 chart-height

                 x
                 y
                 z

                 x-order
                 series-type
                 series
                 interpolation

                 data
                 ))
