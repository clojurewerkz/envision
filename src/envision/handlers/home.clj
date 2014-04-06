(ns envision.handlers.home)

(defn index
  [request]
  {:render :html
   :widgets {:main-content 'envision.widgets.home/index-content
             :includes {:tag :script :attrs {:type "text/jsx" :src "/javascripts/application.js"}}}})
