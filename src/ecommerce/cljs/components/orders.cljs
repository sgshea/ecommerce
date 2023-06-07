(ns ecommerce.cljs.components.orders
  "Orders datagrid"
  (:require
   [reagent.core :as r]
   [ajax.core :refer [GET]]
   [ecommerce.cljs.auth :refer [get-auth-header]]
   [reagent-mui.x.data-grid :refer [data-grid]]
   [reagent-mui.material.grid :refer [grid]]
   [reagent-mui.material.typography :refer [typography]]
   [reagent-mui.util :refer [clj->js']]))

(defonce orders (r/atom nil))

(defonce selected-orders (r/atom nil))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-orders [orders]
  (GET "/api/orders"
    {:headers (conj {"Accept" "application/transit+json"} (get-auth-header))
     :handler #(reset! orders (vec %))
     :error-handler error-handler}))

(defn format-orders-data
  "Formats the orders data from a GET request to rows for the data-table"
  [orders]
  (map #(hash-map :id (:order_details/id %)
                  :order_id (:orders/id %)
                  :username (:users/username %)
                  :product (:products/name %)
                  :quantity (:order_details/quantity %)
                  :price (:products/price %)
                  :time (:orders/order_time %))
       orders))

(defn rows-selection-handler
  "Updates the selected-ids atom with the ids of selected rows"
  [selection-model]
  (reset! selected-orders selection-model)
  (.log js/console (str "Selected ids: " @selected-orders)))

(def columns
  [{:field :id
    :headerName "ID"
    :width 80}
   {:field :order_id
    :headerName "Order ID"
    :width 80}
   {:field :username
    :headerName "Username"
    :width 100}
   {:field :product
    :headerName "Product"
    :width 100}
   {:field :quantity
    :headerName "Quantity"
    :width 150}
   {:field :price
    :headerName "Price"
    :width 80}
   {:field :time
    :headerName "Time ordered"
    :width 200}])

(defn data-grid-component [rows col]
  [data-grid
      {:rows rows
       :columns col
       :auto-height true
       :initial-state (clj->js' {:pagination {:pagination-model {:page-size 25}}})
       :page-size-options [25]
       :checkbox-selection true
       :disable-row-selection-on-click true
       :density :compact
       :on-row-selection-model-change rows-selection-handler}])

(defn initialize-datagrid
  "Helper function"
  []
  [data-grid-component (format-orders-data @orders) columns])

(defn orders-datagrid
  "Main function defining products datagrid"
  []
  (get-orders orders)
  [grid {:mt 5
         :container true
         :align-items "center"
         :justify-content "center"
         :direction "column"}
   [grid {:item true
          :mb 2
          :xs 12}
    [typography {:variant :h4}
    "Orders List"]]
   [grid {:item true}
    [initialize-datagrid]]])