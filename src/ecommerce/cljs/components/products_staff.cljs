(ns ecommerce.cljs.components.products-staff
  "Components to allow staff to manage products"
  (:require
   [reagent.core :as r]
   [ajax.core :refer [GET POST DELETE]]
   [ecommerce.cljs.auth :refer [get-auth-header]]
   [ecommerce.cljs.components.products :refer [initialize-datagrid products selected-products]]
   [reagent-mui.material.grid :refer [grid]]
   [reagent-mui.material.typography :refer [typography]]
   [reagent-mui.icons.add :refer [add] :rename {add add-icon}]
   [reagent-mui.material.text-field :refer [text-field]]
   [reagent-mui.material.button :refer [button]]
   [reagent-mui.icons.delete :refer [delete] :rename {delete delete-icon}]
   [reagent-mui.icons.delete-forever :refer [delete-forever] :rename {delete-forever delete-forever-icon}]
   [reagent-mui.material.dialog :refer [dialog]]
   [reagent-mui.material.dialog-actions :refer [dialog-actions]]
   [reagent-mui.material.dialog-content :refer [dialog-content]]
   [reagent-mui.material.dialog-content-text :refer [dialog-content-text]]
   [reagent-mui.material.dialog-title :refer [dialog-title]]))

(defn event-value
  [e]
  (.. e -target -value))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-products [products]
  (GET "/api/products"
    {:headers (conj {"Accept" "application/transit+json"} (get-auth-header))
     :handler #(reset! products (vec %))
     :error-handler error-handler}))

(defn post-product [product]
  (POST "/api/products"
    {:headers (conj {"Accept" "application/transit+json"} (get-auth-header))
     :params product
     :handler #(.log js/console "Added product: " (clj->js product))
     :error-handler error-handler}))

(defn delete-product [product]
  (DELETE (str "/api/products/" product)
    {:headers (conj {"Accept" "application/transit+json"} (get-auth-header))
     :params {}
     :handler #(.log js/console "Deleted product: " product)
     :error-handler error-handler}))

(defn row-addition-button-dialog
  "Form dialog to add a new product"
  [dialog-open?]
  [:div
     [button {:variant :outlined
              :on-click #(reset! dialog-open? true)
              :start-icon (r/as-element [add-icon])}
      "Add New Product"]
     [dialog {:open @dialog-open?
              :on-close #(reset! dialog-open? false)}
      (let [product (r/atom {:name ""
                             :description ""
                             :category ""
                             :price 0
                             :quantity 0})]
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
                      :multiline true
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
                                   (swap! product assoc-in [:price] (js/parseFloat (event-value e))))
                      :type :number
                      :full-width false
                      :variant :standard}]
         [text-field {:auto-focus false
                      :margin :dense
                      :id :price-field
                      :label "Initial Quantity"
                      :on-change (fn [e]
                                   (swap! product assoc-in [:quantity] (js/parseInt (event-value e))))
                      :type :number
                      :full-width false
                      :variant :standard}]
         [dialog-actions
          [button {:on-click #(reset! dialog-open? false)} "Close"]
          [button {:on-click #(do
                                (reset! dialog-open? false)
                                (post-product @product)
                                (reset! product {:name ""
                                                 :description ""
                                                 :category ""
                                                 :price 0
                                                 :quantity 0})
                                (get-products products))} "Submit"]]])]])

(defn row-deletion-button
  "Button to delete selected items, opens a confirmation dialog"
  [dialog-open?]
  [:div
   [button {:variant :outlined
            :on-click #(reset! dialog-open? true)
            :start-icon (r/as-element [delete-icon])}
    "Delete Selected Products"]
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

(defn products-datagrid-staff
  "Staff member version of datagrid component, allowing product addition, deletion, and updating"
  []
  (let [addition-dialog-open? (r/atom false)
        deletion-dialog-open? (r/atom false)]
    (get-products products)
    [grid {:m 6
           :container true
           :spacing 1
           :justify-content "center"
           :align-items "center"
           :direction "column"}
     [grid {:item true}
      [typography {:variant :h4
                   :mb 2}
       "Products List"]]
     [grid {:item true}
      [typography {:variant :body1
                   :mb 2}
       "Staff are able to add, delete and update products. To update products, click inside the price, quantity, or description."]]
     [grid {:item true
            :xs 4}
      [initialize-datagrid true]]
     [grid {:mt 1
            :container true
            :item true
            :justify-content "center"}
      [grid {:item true}
       [row-addition-button-dialog addition-dialog-open?]]
      [grid {:item true}
       [row-deletion-button deletion-dialog-open?]]]]))