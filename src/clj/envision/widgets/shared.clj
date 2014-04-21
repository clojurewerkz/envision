(ns envision.widgets.shared
  (:require [clojurewerkz.gizmo.widget :refer [defwidget]]
            [envision.snippets.shared :as shared]))

(defwidget header
  :view shared/header-snippet
  :fetch (fn [_]))
