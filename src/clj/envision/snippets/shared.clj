(ns envision.snippets.shared
  (:require [net.cgrand.enlive-html :as html]
            [clojurewerkz.gizmo.enlive :refer [defsnippet within]]))

(defsnippet header-snippet "templates/shared/header.html"
  [*header]
  [env])
