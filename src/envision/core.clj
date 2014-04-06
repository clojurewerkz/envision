(ns envision.core
  (:require [compojure.handler :refer [api]]
            [clojurewerkz.gizmo.responder :refer [wrap-responder]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]

            [envision.routes :as routes]))

(def app

  (-> (api routes/main-routes)
      wrap-responder
      wrap-params
      (wrap-resource "public")
      wrap-content-type
      wrap-reload
      (wrap-stacktrace :color? true)))
