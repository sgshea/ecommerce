(ns ecommerce.clj.controllers.products
  "API controller for product related data"
  (:require [clojure.tools.logging :as log]
            [ecommerce.clj.model.products-model :as model]
            [ring.util.response :as r]))

(defn get-products
  "Returns all of the products"
  [req]
  (let [products (model/get-products (:db req))]
    (r/response products)))

(defn save-new
  "Saves a new product"
  [req]
  (log/info "adding product: " (:body (:parameters req)))
  (model/add-product (:db req)
                   (get-in req [:parameters :body]))
  (r/status 200))

(defn edit
  "Updates a product"
  [req]
  (log/info "editing product: " (:body (:parameters req)))
  (model/update-product (:db req)
                   (get-in req [:parameters :body]))
  (r/status 200))

(defn delete-by-id 
  "Deletes a product given id"
  [req]
  (log/info "deleting product: " (get-in req [:path-params :id]))
  (model/delete-product-by-id (:db req)
                           (get-in req [:path-params :id]))
  (r/status 200))
