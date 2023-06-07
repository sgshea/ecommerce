(ns ecommerce.cljs.components.order-products
  "Standalone module to facilitate ordering based on selected products"
  (:require
   [reagent.core :as r]
   [ajax.core :refer [POST]]
   [ecommerce.cljs.auth :refer [get-auth-header get-auth-state]]
   [ecommerce.cljs.components.orders :as orders]
   [reagent-mui.material.grid :refer [grid]]
   [reagent-mui.material.button :refer [button]]
   [reagent-mui.icons.add :refer [add] :rename {add add-icon}]
   [reagent-mui.material.text-field :refer [text-field]]
   [reagent-mui.material.dialog :refer [dialog]]
   [reagent-mui.material.dialog-actions :refer [dialog-actions]]
   [reagent-mui.material.dialog-content :refer [dialog-content]]
   [reagent-mui.material.dialog-content-text :refer [dialog-content-text]]
   [reagent-mui.material.dialog-title :refer [dialog-title]]
  
  ))
  
(def new-order (r/atom {}))
  
(defn event-value
  [e]
  (.. e -target -value))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn post-order [order]
  (POST "/api/orders"
    {:headers (conj {"Accept" "application/transit+json"} (get-auth-header))
     :params order
     :handler #(.log js/console "Added order: " (clj->js order))
     :error-handler error-handler}))

(defn quantity-selector
  "Select the quantity of a item"
  [product id]
  [grid {:item true
         :m 1
         :key id}
   [text-field {:auto-focus false
                :margin :dense
                :label (str "Quantity for " (:products/name product))
                :on-change (fn [e]
                             (swap! new-order assoc id (js/parseInt (event-value e))))
                :type :number
                :full-width false
                :variant :standard}]])

(defn order-products-button-dialog
  "Form dialog to add a new order"
  [dialog-open? products selected-products]
  (let [auth-state (get-auth-state)
        zip-products (zipmap (map :products/id @products) @products)]
    [:div
     [button {:variant :outlined
              :on-click #(reset! dialog-open? true)
              :start-icon (r/as-element [add-icon])}
      "Add New Order"]
     [dialog {:open @dialog-open?
              :on-close #(reset! dialog-open? false)}
        [dialog-title (str "New order for " (:username auth-state))]
        [dialog-content
         [dialog-content-text
          "Add an order amount for each item selected."]
         [dialog-content-text
          (str "Current order: " (str @new-order))]
         [grid {:container true}
          (map (fn [id]
                 (quantity-selector (get zip-products id) id)) @selected-products)]
         [dialog-actions
          [button {:on-click #(reset! dialog-open? false)} "Close"]
          [button {:on-click #(do
                                (reset! dialog-open? false)
                                (swap! new-order assoc :user_id (:id auth-state))
                                (post-order @new-order)
                                (reset! new-order {})
                                (orders/get-orders orders/orders))} "Submit"]]]]]))