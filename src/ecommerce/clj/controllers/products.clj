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
  [req]
  (log/info "adding product: " (:body (:parameters req)))
  (model/save-product (:db req)
                   (get-in req [:parameters :body]))
  (r/status 200))

(defn delete-by-id [req]
  (log/info "deleting product: " (get-in req [:path-params :id]))
  (model/delete-product-by-id (:db req)
                           (get-in req [:path-params :id]))
  (r/status 200))
