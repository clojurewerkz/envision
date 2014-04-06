(ns envision.snippets.home
    (:require [net.cgrand.enlive-html :as html]
              [clojurewerkz.gizmo.enlive :refer [defsnippet within]]))

(defsnippet index-snippet "templates/home/index.html"
  [*index-content]
  [env])
