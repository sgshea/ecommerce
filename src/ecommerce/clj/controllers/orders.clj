(ns ecommerce.clj.controllers.orders
  "API controller for orders related data"
  (:require [ecommerce.clj.model.orders-model :as model]
            [ring.util.response :as r]
            [clojure.tools.logging :as log]))

(defn get-orders
  "Returns all of the orders"
  [req]
  (let [orders (model/get-orders (:db req))]
    (r/response orders)))

(defn save-new
  "Saves a new order"
  [req]
  (log/info "creating order: " (:body (:parameters req)))
  (model/add-order (:db req)
                   (get-in req [:parameters :body]))
  (r/status 200))

(defn delete-by-id
  "Deletes a order given id"
  [req]
  (log/info "deleting order: " (:get-in req [:path-params :id]))
  (model/delete-order-by-id (:db req)
                            (get-in req [:path-params :id]))
  (r/status 200))