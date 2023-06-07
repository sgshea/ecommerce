(ns ecommerce.clj.model.orders-model
  "Model for orders and order details"
  (:require
   [honey.sql :as hsql]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]))

(def ^:private initial-order-data
  [{:user_id 1}
   {:user_id 1}
   {:user_id 2}])

(def ^:private initial-order-details-data
  [{:order_id 1
    :product_id 1
    :quantity 15}
   {:order_id 1
    :product_id 2
    :quantity 5}
   {:order_id 2
    :product_id 3
    :quantity 10}
   {:order_id 3
    :product_id 5
    :quantity 50}])

(defn populate-orders
  "Creates tables and auto-populates them with initial data"
  [db]
    (try
      (jdbc/execute-one! db
                         (hsql/format {:create-table :orders
                                       :with-columns
                                       [[:id :integer :primary-key]
                                        [:user_id :integer [:not nil]]
                                        [:order_time :datetime :default :current_timestamp]]}))
      (jdbc/execute-one! db
                         (hsql/format {:create-table :order_details
                                       :with-columns
                                       [[:id :integer :primary-key]
                                        [:order_id :integer [:not nil]]
                                        [:product_id :integer [:not nil]]
                                        [:quantity :integer [:not nil]]]}))
      (println "Created database and added order tables!")
      ;; if table creation was successful, it didn't exist before
      ;; so populate it...
      (try
        (doseq [row initial-order-data]
          (sql/insert! db :orders row))
        (doseq [row initial-order-details-data]
          (sql/insert! db :order_details row))
        (println "Populated database with initial data!")
        (catch Exception e
          (println "Exception:" (ex-message e))
          (println "Unable to populate the initial data -- proceed with caution!")))
      (catch Exception e
        (println "Exception:" (ex-message e))
        (println "Looks like the database is already setup?"))))

(defn get-orders
  "Get all orders, joining the username of the user"
  [db]
  (let [init-query
        (sql/query db
                   (hsql/format {:select [:o.*
                                          :od.*
                                          :u.id :u.username
                                          :p.id :p.name :p.price]
                                 :from [[:orders :o]]
                                 :join-by [:left [[:users :u]
                                                  [:= :o.user_id :u.id]]
                                           :join [[:order_details :od]
                                                  [:= :o.id :od.order_id]]
                                           :left [[:products :p]
                                                  [:= :od.product_id :p.id]]]}))
        cleaned-query (map #(dissoc %
                                    ;; remove these keys
                                    :orders/user_id :users/id
                                    :order_details/product_id :products/id
                                    :order_details/order_id) init-query)]
    cleaned-query))

(defn add-order
  "Adds a new order and corresponding order_details"
  [db {:keys [user_id details_array]}]
  (let [order_id (sql/insert! db :orders {:user_id user_id})]
    (map #((-> %
               (conj {:order_id order_id})
               (sql/insert! db :order_details)))
         details_array)))

(defn delete-order-by-id
  "Deletes a order given an id
   Also goes through order_details and deletes corresponding items"
  [db id]
  (sql/delete! db :order_details {:id id}))