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
            [ecommerce.clj.controllers.products :as products]
            [ecommerce.clj.controllers.orders :as orders]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [clojure.java.io :as io]
            [ring.util.response :refer [response]]
            [ecommerce.clj.auth-utils :refer [wrap-jwt-authentication auth-middleware is-staff is-manager default-route]]
            ))

(defn login-handler
  "Serves login.html page (links to clojurescript)"
  [req]
  (response (slurp (io/resource "login.html"))))

(defn home-handler
  "Serves login.html page (links to clojurescript)"
  [req]
  (response (slurp (io/resource "user.html"))))

(defn staff-handler
  "Serves staff.html page (links to clojurescript)"
  [req]
  (response (slurp (io/resource "staff.html"))))

(def middleware-db
  {:name ::db
   :compile (fn [{:keys [db]} _]
              (fn [handler]
                (fn [req]
                  (handler (assoc req :db db)))))})

(defn app [db]
  (ring/ring-handler
   (ring/router 
    [
     ["/" {:get {:middleware [wrap-jwt-authentication default-route]
                 :handler login-handler}}]
     ["/home" {:get {:middleware [wrap-jwt-authentication]
                      :handler home-handler}}]
     ["/staff" {:get {:middleware [wrap-jwt-authentication]
                      :handler staff-handler}}]
     ["/login" {:get {:handler login-handler}}]
      ["/swagger.json"
       {:get {:no-doc true
              :swagger {:info {:title "ecommerce api"}
                        :basePath "/"}
              :handler (swagger/create-swagger-handler)}}] 
     ["/api"
      ["/register" {:post {:parameters {:body {:username string?
                                               :password string?
                                               :email string?
                                               :role_id int?}}
                           :handler users/register}}]
      ["/login" {:post {:parameters {:body {:username string?
                                            :password string?}}
                        :handler users/login}}]
      ["/me" {:get {:middleware [wrap-jwt-authentication auth-middleware]
                    :handler users/me}}]
      ["/users" {:get {:middleware [wrap-jwt-authentication is-manager]
                       :handler users/get-users}}]
      ["/users/:id" {:delete {:middleware [wrap-jwt-authentication is-manager]
                              :parameters {:path {:id int?}}
                              :handler users/delete-by-id}}]
      ["/products" {:get {:middleware [wrap-jwt-authentication auth-middleware]
                          :handler products/get-products}
                    :post {:middleware [wrap-jwt-authentication is-staff]
                           :parameters {:body {:name string?
                                               :description string?
                                               :category string?
                                               :price number?
                                               :quantity int?}}
                           :handler products/save-new}
                    :put {:middleware [wrap-jwt-authentication is-staff]
                          :parameters {:body {:id int?
                                              :name string?
                                              :description string?
                                              :category string?
                                              :price number?
                                              :quantity int?}}
                          :handler products/edit}}]
      ["/products/:id" {:delete {:middleware [wrap-jwt-authentication is-staff]
                                 :parameters {:path {:id int?}}
                                 :handler products/delete-by-id}}]
      ["/orders" {:get {:middleware [wrap-jwt-authentication auth-middleware]
                        :handler orders/get-orders}
                  :post {:middleware [wrap-jwt-authentication auth-middleware]
                         :handler orders/save-new}}]
      ["/orders/:id" {:delete {:middleware [wrap-jwt-authentication is-staff]
                               :parameters {:path {:id int?}}
                               :handler orders/delete-by-id}}]]]
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
                         ;; auth
                         wrap-keyword-params
                         middleware-db]}})
   (ring/routes
    (swagger-ui/create-swagger-ui-handler {:path "/api"})
    (ring/create-resource-handler
     {:path "/"})
    (ring/create-default-handler
     {:not-found (constantly {:status 404 :body "Not found"})}))))
