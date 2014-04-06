(ns envision.services.nrepl
  ^{:doc "Nrepl service"}
  (:use [clojurewerkz.gizmo.service])
  (:require [clojure.tools.nrepl.server :as nrepl]
            [clojurewerkz.gizmo.config :as config]))

(defservice nrepl-service
  :config #(:nrepl config/settings)
  :alive (fn [service]
           (let [server (state service)]
             (and server
                  (not (.isClosed (.server-socket server))))))
  :stop (fn [service]
          (nrepl/stop-server (state service)))
  :start (fn [service]
           (reset-state service
                        (let [{:keys [port]} (config service)]
                          (nrepl/start-server :port 4555)))))
