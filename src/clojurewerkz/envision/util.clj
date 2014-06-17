(ns clojurewerkz.envision.util)

(defn deep-merge-with
  "Deep merge, taken from: https://github.com/richhickey/clojure-contrib/blob/2ede388a9267d175bfaa7781ee9d57532eb4f20f/src/main/clojure/clojure/contrib/map_utils.clj"
  [f & maps]
  (apply
    (fn m [& maps]
      (if (every? map? maps)
        (apply merge-with m maps)
        (apply f maps)))
    maps))

(defn deep-merge
  [& maps]
  (apply deep-merge-with (fn [x y] y) maps))
