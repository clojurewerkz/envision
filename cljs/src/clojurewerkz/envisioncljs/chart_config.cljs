(ns clojurewerkz.envisioncljs.chart_config
  (:require-macros [schema.macros :as sm])
  (:require [schema.core             :as s]))

(sm/defrecord AxisConfig
    [^{:s s/Any} category-fields
     ^{:s s/Any} colors
     ^{:s s/Any} clamp
     ^{:s s/Any} font-size
     ^{:s s/Any} font-family
     ^{:s s/Any} gridline-shapes
     ^{:s s/Any} hidden
     ^{:s s/Any} log-base
     ^{:s s/Any} use-log
     ^{:s s/Any} measure
     ^{:s s/Any} override-min
     ^{:s s/Any} show-gridlines
     ^{:s s/Any} show-percent
     ^{:s s/Any} title-shape
     ^{:s s/Any} tick-format
     ^{:s s/Any} time-field
     ^{:s s/Any} title
     ^{:s s/Any} floating-bar-width
     ^{:s s/Any} date-parse-format
     ^{:s s/Any} ticks
     ^{:s s/Any} time-period
     ^{:s s/Any} time-interval
     ^{:s s/Any} order-rule
     ^{:s s/Any} group-order-rule])

(defn make-axis-config
  [{:keys [category-fields
           colors
           clamp
           font-size
           font-family
           gridline-shapes
           hidden
           log-base
           use-log
           measure
           override-min
           show-gridlines
           show-percent
           title-shape
           tick-format
           time-field
           title
           floating-bar-width
           date-parse-format
           ticks
           time-period
           time-interval
           order-rule
           group-order-rule]}]

  (->AxisConfig category-fields
                colors
                clamp
                font-size
                font-family
                gridline-shapes
                hidden
                log-base
                use-log
                measure
                override-min
                show-gridlines
                show-percent
                title-shape
                tick-format
                time-field
                title
                floating-bar-width
                date-parse-format
                ticks
                time-period
                time-interval
                order-rule
                group-order-rule))

(sm/defrecord ChartConfig
    [^{:s s/String}  id

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
     ^{:s s/Keyword} series
     ^{:s s/Keyword} interpolation

     ^{:s s/Any}     data
     ^{:s s/Any}     additional-series
     ^{:s s/Str}     headline])

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
           x-config

           series-type
           series
           interpolation

           data
           additional-series
           headline]

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
                 (or (keyword x-type) :category)
                 y
                 (or (keyword y-type) :measure)
                 z

                 (make-axis-config x-config)
                 (make-axis-config y-config)

                 (keyword series-type)
                 series
                 (keyword interpolation)

                 (clj->js data)
                 additional-series
                 headline))
