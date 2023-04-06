(ns ecommerce.clj.handler
  (:require [reitit.ring :as ring]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.ring.coercion :as coercion]
            [reitit.coercion.spec]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart] 
            [muuntaja.core :as m]
            [ecommerce.clj.controllers.user :as users]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [clojure.java.io :as io]
            [ring.util.response :as response]))

(defn default-handler
  "Serves the default.html page (links to clojurescript)"
  [request]
  (response/response (slurp (io/resource "index.html"))))

(def middleware-db
  {:name ::db
   :compile (fn [{:keys [db]} _]
              (fn [handler]
                (fn [req]
                  (handler (assoc req :db db)))))})

(defn app [db]
  (ring/ring-handler
   (ring/router 
    [["/" {:get {:handler default-handler}}]
     ["/swagger.json"
      {:get {:no-doc true
             :swagger {:info {:title "ecommerce api"}
                       :basePath "/api"}
             :handler (swagger/create-swagger-handler)}}]
     ["/users" {:get {:handler users/get-users}
                :post {:parameters {:form {:first_name string?
                                           :last_name string?
                                           :email string?
                                           :role_id int?}}
                       :handler users/save}
                :put {:handler users/save}}]
     ["/users/:id" {:delete {:parameters {:path {:id int?}}
                            :handler users/delete-by-id}}]]
    {:data {:db db
            :coercion reitit.coercion.spec/coercion
            :muuntaja m/instance
            :middleware [;; query-params & form-params
                         parameters/parameters-middleware
                         ;; content-negotiation
                         muuntaja/format-negotiate-middleware
                         ;; encoding response body
                         muuntaja/format-response-middleware
                           ;; exception handling
                         exception/exception-middleware
                           ;; decoding request body
                         muuntaja/format-request-middleware
                           ;; coercing response bodys
                         coercion/coerce-response-middleware
                           ;; coercing request parameters
                         coercion/coerce-request-middleware
                           ;; multipart
                         multipart/multipart-middleware
                         wrap-keyword-params
                         middleware-db]}})
   (ring/routes
    (swagger-ui/create-swagger-ui-handler {:path "/api"})
    (ring/create-resource-handler
     {:path "/"})
    (ring/create-default-handler
     {:not-found (constantly {:status 404 :body "Not found"})}))))
