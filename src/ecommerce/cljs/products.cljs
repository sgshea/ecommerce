(ns ecommerce.cljs.products
  (:require 
   [reagent.core :as r]
   [ajax.core :refer [GET POST PUT DELETE]]
   [reagent-mui.material.button :refer [button]]
   [reagent-mui.x.data-grid :refer [data-grid]]
   [reagent-mui.util :refer [clj->js']]
   [reagent-mui.icons.add :refer [add] :rename {add add-icon}]
   [reagent-mui.icons.delete :refer [delete] :rename {delete delete-icon}]
   [reagent-mui.icons.delete-forever :refer [delete-forever] :rename {delete-forever delete-forever-icon}]
   [reagent-mui.material.text-field :refer [text-field]]
   [reagent-mui.material.dialog :refer [dialog]]
   [reagent-mui.material.dialog-actions :refer [dialog-actions]]
   [reagent-mui.material.dialog-content :refer [dialog-content]]
   [reagent-mui.material.dialog-content-text :refer [dialog-content-text]]
   [reagent-mui.material.dialog-title :refer [dialog-title]]))
   

(defonce products (r/atom nil))

(defonce selected-products (r/atom []))

(defn event-value
  [e]
  (.. e -target -value))

(defn handler [response]
  (.log js/console (str response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-products [products]
  (GET "/api/products"
    {:headers {"Accept" "application/transit+json"}
     :handler #(reset! products (vec %))}))

(defn post-product [product]
  (POST "/api/products"
    {:headers {"Accept" "application/transit+json"}
     :params product
     :handler handler}))

;; (defn put-user [user]
;;   (PUT "/api/users"
;;     {:headers {"Accept" "application/transit+json"}
;;      :params user
;;      :handler handler}))

(defn delete-product [product]
  (DELETE (str "/api/products/" product)
    {:headers {"Accept" "application/transit+json"}
     :params {}
     :handler handler
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

(defn row-deletion-button
  "Button to delete selected items, opens a confirmation dialog"
  [dialog-open?]
  [:div
   [button {:variant :outlined
            :on-click #(reset! dialog-open? true)
            :start-icon (r/as-element [delete-icon])}
    "Delete Selected Users"]
   [dialog {:open @dialog-open?
            :on-close #(reset! dialog-open? false)}
    [dialog-title "Are you sure you want to delete these products"]
    [dialog-content
     [dialog-content-text
      (str "Ids of products which will be deleted: " @selected-products)]
     [dialog-actions
      [button {:on-click #(reset! dialog-open? false)} "Go back"]
      [button {:start-icon (r/as-element [delete-forever-icon])
               :on-click #(do
                            (reset! dialog-open? false)
                            (doseq [product @selected-products]
                              (delete-product product))
                            (get-products products))} "Delete Products"]]]]])

(defn row-update
  "Updates a row, used for the datagrid"
  [new]
  ;; (put-user (format-user-data-back (js->clj new :keywordize-keys true)))
  (get-products products)
  new)

(defn row-update-error [error]
  (.log js/console (str error)))

(defn product-dialog
  "Form dialog to add a new product"
  [dialog-open?]
  (let [product (r/atom {:name ""
                         :description ""
                         :category ""
                         :price 0
                         :quantity 0})]
    [:div
     [button {:variant :outlined
              :on-click #(reset! dialog-open? true)
              :start-icon (r/as-element [add-icon])}
      "Add New Product"]
     [dialog {:open @dialog-open?
              :on-close #(reset! dialog-open? false)}
      [dialog-title "Add New Product"]
      [dialog-content
       [dialog-content-text
        "To add a new product, please enter a name, description, category, price, and initial amount."]
       [text-field {:auto-focus true
                    :margin :dense
                    :id :name-field
                    :label "Product Name"
                    :on-change (fn [e]
                                 (swap! product assoc-in [:name] (event-value e)))
                    :type :text
                    :full-width true
                    :variant :standard}]
       [text-field {:auto-focus false
                    :margin :dense
                    :id :description-field
                    :label "Product Description"
                    :on-change (fn [e]
                                 (swap! product assoc-in [:description] (event-value e)))
                    :type :text
                    :full-width true
                    :variant :standard}]
       [text-field {:auto-focus false
                    :margin :dense
                    :id :category-field
                    :label "Category"
                    :on-change (fn [e]
                                 (swap! product assoc-in [:category] (event-value e)))
                    :type :text
                    :full-width true
                    :variant :standard}]
       [text-field {:auto-focus false
                    :margin :dense
                    :id :price-field
                    :label "Price"
                    :on-change (fn [e]
                                 (swap! product assoc-in [:price] (event-value e)))
                    :type :number
                    :full-width false
                    :variant :standard}]
       [text-field {:auto-focus false
                    :margin :dense
                    :id :price-field
                    :label "Initial Quantity"
                    :on-change (fn [e]
                                 (swap! product assoc-in [:quantity] (event-value e)))
                    :type :number
                    :full-width false
                    :variant :standard}]
       [dialog-actions
        [button {:on-click #(reset! dialog-open? false)} "Close"]
        [button {:on-click #(do
                              (post-product @product)
                              (reset! product {:name ""
                                               :description ""
                                               :category ""
                                               :price 0
                                               :quantity 0})
                              (reset! dialog-open? false)
                              (get-products products))} "Submit"]]]]]))

(def columns [{:field :id
               :headerName "ID"
               :width 80}
              {:field :name
               :headerName "Name"
               :width 100}
              {:field :description
               :headerName "Description"
               :width 300}
              {:field :category
               :headerName "Category"
               :width 130}
              {:field :price
               :headerName "Price"
               :width 80
               :editable false}
              {:field :quantity
               :headerName "Quantity"
               :width 150
               :editable false}])

(defn data-grid-component [rows col]
  [:div {:style {:height 400 :width 800}}
   [data-grid {:rows rows
               :columns col
               :initial-state (clj->js' {:pagination {:pagination-model {:page-size 5}}})
               :page-size-options [5]
               :checkbox-selection true
               :disable-row-selection-on-click true
               :density :standard
               :process-row-update row-update
               :on-process-row-update-error row-update-error
               :on-row-selection-model-change rows-selection-handler
               }]])

(defn products-page 
  "Main function defining products page"
  []
  (let [add-product-dialog (r/atom false)
        remove-product-dialog (r/atom false)]
    (get-products products)
    (fn []
      [:div
       [:h3 "Products List"]
       [data-grid-component (format-products-data @products) columns]
       [product-dialog add-product-dialog]
       [row-deletion-button remove-product-dialog]])))