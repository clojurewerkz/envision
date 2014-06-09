(ns clojurewerkz.envisioncljs.line_chart
  (:require-macros [schema.macros :as sm])
  (:require [schema.core             :as s]))


(sm/defrecord ChartConfig
    [^{:s s/String}  id

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
                 interpolation

                 data
                 ))
