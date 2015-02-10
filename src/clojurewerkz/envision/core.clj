(ns clojurewerkz.envision.core
  (:import [org.apache.commons.io FileUtils]
           [sun.net.www.protocol.jar JarURLConnection$JarURLInputStream]
           [sun.net.www.content.text PlainTextInputStream])

  (:require [cheshire.core                              :as json]
            [clojure.java.io                            :as io]
            [schema.core                                :as s]
            [clojurewerkz.envision.chart-config         :as cfg]

            [clojurewerkz.envision.util                 :refer [deep-merge add-serial-ids]]

            [clojurewerkz.statistiker.clustering.kmeans :as km]
            [clojurewerkz.statistiker.clustering.dbscan :as dbs]
            [clojurewerkz.statistiker.histograms        :as hist]
            [clojurewerkz.statistiker.regression        :as regression]
            [clojurewerkz.statistiker.distribution      :as distribution]

            [clojure.java.browse :refer [browse-url]]))

(defn temp-name
  "Create a temporary file name like what is created for [[temp-file]]
   and [[temp-dir]]."
  ([prefix] (temp-name prefix ""))
  ([prefix suffix]
     (format "%s%s-%s%s" prefix (System/currentTimeMillis)
             (long (rand 0x100000000)) suffix)))

(defn extract-resources [dest-path filename]
  (cond
   (or (= PlainTextInputStream
          (type (.getContent (io/resource "template.project.clj"))))
       (= java.io.BufferedInputStream
          (type (.getContent (io/resource "template.project.clj")))))
   (let [file (io/file (clojure.java.io/resource filename))]
     (if (.isDirectory file)
       (FileUtils/copyDirectoryToDirectory
        (io/file file)
        (io/file dest-path))
       (FileUtils/copyFileToDirectory
        (io/file file)
        (io/file dest-path))))

   (= sun.net.www.protocol.jar.JarURLConnection$JarURLInputStream
      (type (.getContent (io/resource "template.project.clj"))))

   (let [zip-path (-> (io/resource "template.project.clj")
                      (.getPath )
                      (.replace "!/template.project.clj" "")
                      (.replace "file:" ""))
         zip-file (java.util.zip.ZipFile. zip-path)]
     (doseq [zip-entry (enumeration-seq (.entries zip-file))]
       (let [entry-name (.getName zip-entry)]
         (cond
          (.isDirectory zip-entry)
          (FileUtils/forceMkdir (io/file entry-name))

          (.startsWith entry-name filename)
          (FileUtils/copyInputStreamToFile (.getInputStream zip-file zip-entry)
                                           (io/file (str dest-path entry-name)))))))

   :else
   (throw (Exception. "Don't know how to extract resources."))
   ))

(defn render
  "Prepares a tmp directory with all templates and returns a path to it"
  [data]
  (let [dest-path (str (FileUtils/getTempDirectoryPath) (temp-name "envision-") "/")
        index     (str dest-path "/index.html")]

    (FileUtils/forceMkdir (io/file dest-path))

    (extract-resources dest-path "assets")
    (extract-resources dest-path "templates")
    (extract-resources dest-path "src")
    (extract-resources dest-path "template.project.clj")

    (FileUtils/moveDirectoryToDirectory
     (io/file (str dest-path "/src" ))
     (io/file (str dest-path "/cljs/src"))
     true)

    (FileUtils/moveFile
     (io/file (str dest-path "template.project.clj"))
     (io/file (str dest-path "/cljs/project.clj")))

    (spit (str dest-path "/assets/data/data.js")
          (str "var renderData = "(json/generate-string data) ";"))

    dest-path
    ;; (browse-url (str "file://" index))
    ))

(defn histogram
  "Histogram accepts a vector of values"
  ([bins data]
     (histogram bins data))
  ([bins data config-overrides]
     (let [hist (->> (hist/empirical-distribution bins data)
                     (map (fn [[x y]] {:x (format "%.3f" x) :y y})))]
       (cfg/make-chart-config
        (deep-merge
         {:id          "histogram"
          :headline    "Histogram"
          :x           "x"
          :x-config    {:order-rule :x}
          :y           "y"
          :series-type "bar"
          :data        hist}
         config-overrides)))))

(defn linear-regression
  [data x-field y-field series & config-overrides]
  (let [d             (map x-field data)
        [min-x max-x] [(apply min d) (apply max d)]]
    (cfg/make-chart-config
     (deep-merge
      {:id                "bubble"
       :headline          "Linear Regression"
       :x                 x-field
       :x-type            :measure
       :y                 y-field
       :y-type            :measure
       :x-config          {:order-rule   x-field
                           :override-min min-x}
       :series-type       :bubble
       :data              data
       :series            series
       :additional-series [:linear-trend {:data (let [{:keys [intercept slope]} (regression/linear-regression
                                                                                 data x-field y-field)]

                                                  [{:cx min-x :cy (+ intercept (* slope min-x))},
                                                   {:cx max-x :cy (+ intercept (* slope max-x))}])}]
       }
      (or (first config-overrides) {})))))


(defn kmeans
  "Only first two fields will be visualised. All fields should be measure fields."
  [data fields k iterations & config-overrides]
  (let [clusters (km/cluster-by data fields k iterations)]
    (cfg/make-chart-config
     (deep-merge
      {:id          "kmeans"
       :headline    "K-Means Clusters"
       :x           (first fields)
       :y           (second fields)
       :x-type      :measure
       :y-type      :measure
       :series-type :bubble
       :data        clusters
       :series      (conj fields :cluster-id)}
      (or (first config-overrides) {})))))

(defn dbscan
  [data fields eps min-points & config-overrides]
  (let [clusters (dbs/cluster-by data fields eps min-points)]
    (cfg/make-chart-config
     (deep-merge
      {:id          "dbscan"
       :headline    "DBScan Clusters"
       :x           (first fields)
       :y           (second fields)
       :x-type      :measure
       :y-type      :measure
       :series-type :bubble
       :data        clusters
       :series      [(first fields) (second fields) :cluster-id]}
      (or (first config-overrides) {})))))


(defn a []
  (render
   [(dbscan  [{:a 1 :b 1 :c 1}
              {:a 2 :b 2 :c 2}
              {:a 3 :b 3 :c 3}
              {:a 50 :b 50 :c 50}
              {:a 51 :b 51 :c 51}
              {:a 53 :b 53 :c 53}
              {:a 54 :b 54 :c 54}]
             [:a :b]
             2.0
             1)])

  (render
   [(kmeans  [{:a 1 :b 1 :c 1}
              {:a 2 :b 2 :c 2}
              {:a 3 :b 3 :c 3}
              {:a 50 :b 50 :c 50}
              {:a 51 :b 51 :c 51}
              {:a 53 :b 53 :c 53}
              {:a 54 :b 54 :c 54}]
             [:a :b :c]
             2
             100)])

  (render
   [(cfg/make-chart-config
     {:id            "line"
      :headline      "Line Chart"
      :x             :timestamp
      :y             :value
      :x-config      {:order-rule "time"
                      :tick-format "%H:%M"}
      :y-type        :measure
      :x-type        :time
      :series-type   "line"
      ;; :data          (json/decode (slurp "/Users/ifesdjeen/hackage/continuum/result.json") true)
      :series        :subtype
      :interpolation :cardinal
      })]
   )

  (render
   [(histogram 10 (take 100 (distribution/normal-distribution 5 10))
               {:tick-format "s"})

    (linear-regression
     (flatten (for [i (range 0 20)]
                [{:year (+ 2000 i)
                  :income (+ 10 i (rand-int 10))
                  :series "series-1"}
                 {:year (+ 2000 i)
                  :income (+ 10 i (rand-int 20))
                  :series "series-2"}]
                ))
     :year
     :income
     [:year :income :series])
    (cfg/make-chart-config
     {:id            "line"
      :headline      "Line Chart"
      :x             "year"
      :y             "income"
      :x-config      {:order-rule "year"}
      :series-type   "line"
      :data          (flatten (for [i (range 0 20)]
                                [{:year (+ 2000 i)
                                  :income (+ 10 i (rand-int 10))
                                  :series "series-1"}
                                 {:year (+ 2000 i)
                                  :income (+ 10 i (rand-int 20))
                                  :series "series-2"}]
                                ))
      :series        "series"
      :interpolation :cardinal
      })
    (cfg/make-chart-config
     {:id            "area"
      :headline      "Area Chart"
      :x             "year"
      :y             "income"
      :x-config      {:order-rule "year"}
      :series-type   "area"
      :data          (into [] (for [i (range 0 20)] {:year (+ 2000 i) :income (+ 10 i (rand-int 10))}))
      :interpolation :cardinal
      })
    ]))

(comment
  (-> (io/resource "assets")
      (.getPath )
      (.replace "!/assets" "")
      (.replace "file:" "")
      ))
