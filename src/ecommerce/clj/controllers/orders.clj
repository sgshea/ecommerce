(ns ecommerce.clj.controllers.orders
  "API controller for orders related data"
  (:require [ecommerce.clj.model.orders-model :as model]
            [ring.util.response :as r]
            [clojure.tools.logging :as log]))

(defn get-orders
  "Returns all of the orders,
   checks that if the user is a customer, they only get their own orders"
  [req]
  (let [role-id (get-in req [:identity :role_id])
        user-id (get-in req [:identity :id])]
    (case role-id
      1 (r/response (model/get-orders (:db req)))
      2 (r/response (model/get-orders (:db req)))
      (r/response (model/get-orders-for-customer (:db req) user-id)))))

(defn save-new
  "Saves a new order"
  [req]
  (log/info "creating order: " (:body-params req) " for user " (:identity req))
  (model/add-order (:db req)
                   (:body-params req)
                   (get-in req [:identity :id]))
  (r/status 200))

(defn delete-by-id
  "Deletes a order given id"
  [req]
  (log/info "deleting order: " (get-in req [:path-params :id]))
  (model/delete-order-by-id (:db req)
                            (get-in req [:path-params :id]))
  (r/status 200))