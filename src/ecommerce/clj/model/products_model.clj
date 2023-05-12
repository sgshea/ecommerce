(ns ecommerce.clj.model.products-model
  "Model for products data"
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [honey.sql :as hsql]
            [honey.sql.helpers :as h :refer [select from where order-by over]]))

(def ^:private initial-product-data
  [{:name "Apples" :description "Red Fruit" :category "Fruit" :price 4.5 :quantity 100}
   {:name "Bananas" :description "Yellow Fruit" :category "Fruit" :price 3.0 :quantity 50}
   {:name "Oranges" :description "Citrus Fruit" :category "Fruit" :price 2.5 :quantity 75}
   {:name "Carrots" :description "Root Vegetable" :category "Vegetable" :price 2.0 :quantity 80}
   {:name "Spinach" :description "Leafy Green" :category "Vegetable" :price 1.5 :quantity 90}
   {:name "Tomatoes" :description "Red Vegetable" :category "Vegetable" :price 3.5 :quantity 70}
   {:name "Chicken" :description "Lean Meat" :category "Meat" :price 6.0 :quantity 40}
   {:name "Beef" :description "Grass-fed Meat" :category "Meat" :price 8.0 :quantity 30}
   {:name "Salmon" :description "Fatty Fish" :category "Fish" :price 9.5 :quantity 25}
   {:name "Pasta" :description "Italian Staple" :category "Grains" :price 2.5 :quantity 60}
   {:name "Rice" :description "Versatile Grain" :category "Grains" :price 3.0 :quantity 70}])

(defn populate-products
  "Creates tables and auto-populates them with initial data"
  [db]
    (try
      (jdbc/execute-one! db
                         (hsql/format {:create-table :products
                                       :with-columns
                                       [[:id :integer :primary-key]
                                        [:name [:varchar 32] [:not nil]]
                                        [:description [:varchar 255]]
                                        [:category [:varchar 32] [:not nil]]
                                        [:price :float [:not nil]]
                                        [:quantity :int [:not nil]]]}))
      (println "Created database and added product table!")
      ;; if table creation was successful, it didn't exist before
      ;; so populate it...
      (try
        (doseq [row initial-product-data]
          (sql/insert! db :products row))
        (println "Populated database with initial data!")
        (catch Exception e
          (println "Exception:" (ex-message e))
          (println "Unable to populate the initial data -- proceed with caution!")))
      (catch Exception e
        (println "Exception:" (ex-message e))
        (println "Looks like the database is already setup?"))))

(defn get-products
  "Return all of the products"
  [db]
  (sql/query db
             (hsql/format {:select [:*]
                           :from [:products]})))

(defn add-product
  "Adds a single product"
  [db product]
  (sql/insert! db :products product))

(defn update-product
  "Updates a single product"
  [db product]
  (let [id (:id product)]
    (sql/update! db :products
                (dissoc product :products/id)
                {:id id})))

(defn delete-product-by-id
  "Deletes a product given an id"
  [db id]
  (sql/delete! db :products {:id id}))