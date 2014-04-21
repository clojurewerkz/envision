(ns envision.utils)

(defn dom-node
  [t]
  (.getDOMNode t))

(def data-field #(-> %1 .-attributes (.getNamedItem (name %2)) (.-nodeValue)))
