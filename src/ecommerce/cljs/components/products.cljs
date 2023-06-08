(ns ecommerce.cljs.components.products
  "Products datagrid"
  (:require
   [reagent.core :as r]
   [ajax.core :refer [GET PUT]]
   [ecommerce.cljs.auth :refer [get-auth-header]]
   [ecommerce.cljs.components.order-products :refer [order-products-button-dialog error-state]]
   [reagent-mui.x.data-grid :refer [data-grid]]
   [reagent-mui.material.grid :refer [grid]]
   [reagent-mui.material.typography :refer [typography]]
   [reagent-mui.util :refer [clj->js']]))

(defonce products (r/atom nil))

(defonce selected-products (r/atom nil))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-products [products]
  (GET "/api/products"
    {:headers (conj {"Accept" "application/transit+json"} (get-auth-header))
     :handler #(reset! products (vec %))
     :error-handler error-handler}))

(defn put-product [product]
  (PUT "/api/products"
    {:headers (conj {"Accept" "application/transit+json"} (get-auth-header))
     :params product
     :handler #(.log js/console (str "Updated product " product))
     :error-handler error-handler}))

(defn format-products-data
  "Formats the product data from a GET request to rows for the data-table"
  [products]
  (map #(hash-map :id (:products/id %)
                  :name (:products/name %)
                  :description (:products/description %)
                  :category (:products/category %)
                  :price (:products/price %)
                  :quantity (:products/quantity %))
       products))

(defn rows-selection-handler
  "Updates the selected-ids atom with the ids of selected rows"
  [selection-model]
  (reset! selected-products selection-model)
  (.log js/console (str "Selected ids: " @selected-products)))

(defn row-update
  "Updates a row, used for the datagrid"
  [new]
  (->
   (js->clj new :keywordize-keys true)
   (update :price js/parseFloat)
   (update :quantity js/parseInt)
   (put-product))
  (get-products products)
  new)

(defn row-update-error [error]
  (.log js/console (str error)))

(defn columns
  [staff?]
  [{:field :id
    :headerName "ID"
    :width 80}
   {:field :name
    :headerName "Name"
    :width 100}
   {:field :description
    :headerName "Description"
    :width 300
    :editable staff?}
   {:field :category
    :headerName "Category"
    :width 130}
   {:field :price
    :headerName "Price"
    :width 80
    :editable staff?}
   {:field :quantity
    :headerName "Quantity"
    :width 150
    :editable staff?}])

(defn data-grid-component [rows col staff?]
  [data-grid
   (cond->
      {:rows rows
       :columns col
       :auto-height true
       :initial-state (clj->js' {:pagination {:pagination-model {:page-size 25}}})
       :page-size-options [25]
       :checkbox-selection true
       :disable-row-selection-on-click true
       :density :compact
       :on-row-selection-model-change rows-selection-handler}
     staff?
     (conj {:process-row-update row-update
            :on-process-row-update-error row-update-error}))])

(defn initialize-datagrid
  "Helper function"
  [staff?]
  [data-grid-component (format-products-data @products) (columns staff?) staff?])

(defn products-datagrid
  "Main function defining products datagrid"
  []
  (let [order-dialog-open? (r/atom false)]
    (get-products products)
    [grid {:mt 5
           :container true
           :spacing 1
           :align-items "center"
           :justify-content "center"
           :direction "column"}
     [grid {:item true
            :xs 12}
      [typography {:variant :h4}
       "Products List"]]
     [grid {:item true}
      [typography {:variant :body1}
       "Select Products to add them to order list."]]
     [grid {:item true}
      [initialize-datagrid false]]
     [grid {:item true}
      [typography {:variant :body1}
       @error-state]]
     [grid {:item true}
      [order-products-button-dialog order-dialog-open? products selected-products get-products]]]))