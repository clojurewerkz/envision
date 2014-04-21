(ns envision.widgets.home
    (:require [clojurewerkz.gizmo.widget :refer [defwidget]]
              [envision.snippets.home :as snippets]))

(defwidget index-content
  :view snippets/index-snippet
  :fetch (fn [_]))
