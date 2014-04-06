(ns envision.generator
  (:import [java.util UUID Date])
  (:import [org.apache.commons.math3.distribution
            NormalDistribution
            LogNormalDistribution
            PoissonDistribution])
  (:require [clojurewerkz.statistiker.distribution :refer :all]))

(def minute (* 60 1000))
(def second (* 1 1000))

(defn generate-ip-address
  []
  (str (rand-int 255) "."
       (rand-int 255) "."
       (rand-int 255) "."
       (rand-int 255)))

(defn generate-session-id
  []
  (str (UUID/randomUUID)))

(def user-agents
  ["Mozilla/5.0 (iPhone; CPU iPhone OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B179 Safari/7534.48.3"
   "Mozilla/5.0 (iPod; CPU iPhone OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3"
   "Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3"
   "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_4) AppleWebKit/536.25 (KHTML, like Gecko) Version/6.0 Safari/536.25"
   "Mozilla/5.0 (Windows; Windows NT 6.1) AppleWebKit/534.57.2 (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2"
   "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)"
   "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0)"
   "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)"
   "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_4) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.46 Safari/536.5"
   "Mozilla/5.0 (Windows; Windows NT 6.1) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.46 Safari/536.5"
   "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:11.0) Gecko/20100101 Firefox/11.0"
   "Mozilla/5.0 (Windows NT 6.1; rv:11.0) Gecko/20100101 Firefox/11.0"
   "Opera/9.80 (Macintosh; Intel Mac OS X 10.7.4; U; en) Presto/2.10.229 Version/11.62"
   "Opera/9.80 (Windows NT 6.1; U; en) Presto/2.10.229 Version/11.62"])

(defn generate-user-agent
  []
  (get user-agents (rand-int (dec (count user-agents)))))

(defn generate-users
  [start-time sessions gen]
  (let [gen-next (fn [] (let [[ip id] (nth sessions (rand-int (count sessions)))]
                         [(+ start-time (int (.sample gen)))
                          ip
                          id
                          "user"]))]

    ((fn sample []
       (cons
        (gen-next)
        (lazy-seq (sample)))))))

(defn generate-crawlers
  [start-time ips gen]
  (let [gen-next (fn []  [(+ start-time (int (.sample gen)))
                         (nth ips (rand-int (count ips)))
                         (generate-session-id)
                         "crawler"])]
    ((fn sample []
       (cons
        (gen-next)
        (lazy-seq (sample)))))))

(defn generate-bots
  [start-time  ips gen]
  (let [gen-next (fn []  [(+ start-time (int (.sample gen)))
                         (nth ips (rand-int (count ips)))
                         (generate-session-id)
                         "bot"])]
    ((fn sample []
       (cons
        (gen-next)
        (lazy-seq (sample)))))))

(defn generate-all
  [amount minutes]
  (let [base                  (.getTime (Date.))
        users-amount          10
        bots-amount           5
        crawlers-amount       3
        user-sessions         (map vector
                                   (take users-amount (repeatedly generate-ip-address))
                                   (take users-amount (repeatedly generate-session-id)))
        crawler-ips           (take crawlers-amount (repeatedly generate-ip-address))
        bot-ips               (take bots-amount (repeatedly generate-ip-address))
        user-time-diff-gen    (NormalDistribution. (* 30 second)
                                                   (* 30 second))
        crawler-time-diff-gen (NormalDistribution. (* 10 second)
                                                   (* 1 second))
        bots-time-diff-gen    (NormalDistribution. (* 5 second)
                                                   (* 2 second))
        records               (into []
                                    (for [i (range 0 minutes)]
                                      (let [start-time (+ base (* i minute))
                                            users      (take amount (generate-users start-time user-sessions user-time-diff-gen))
                                            bots       (take amount (generate-bots start-time bot-ips bots-time-diff-gen))
                                            crawlers   (take amount (generate-crawlers start-time crawler-ips bots-time-diff-gen))]
                                        (->> (concat users bots crawlers)
                                             (map (fn [[time ip session whoami]]
                                                    {:time    time
                                                     :ip      ip
                                                     :session session
                                                     :whoami  whoami}
                                                    ))))))]
    (->> records
         flatten
         (sort-by :time))))
