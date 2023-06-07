(ns ecommerce.cljs.components.orders-staff
  (:require
   [reagent.core :as r]
   [ajax.core :refer [GET DELETE]]
   [ecommerce.cljs.auth :refer [get-auth-header]]
   [ecommerce.cljs.components.orders :refer [initialize-datagrid orders selected-orders]]
   [reagent-mui.material.grid :refer [grid]]
   [reagent-mui.material.typography :refer [typography]]
   [reagent-mui.material.button :refer [button]]
   [reagent-mui.icons.delete :refer [delete] :rename {delete delete-icon}]
   [reagent-mui.icons.delete-forever :refer [delete-forever] :rename {delete-forever delete-forever-icon}]
   [reagent-mui.material.dialog :refer [dialog]]
   [reagent-mui.material.dialog-actions :refer [dialog-actions]]
   [reagent-mui.material.dialog-content :refer [dialog-content]]
   [reagent-mui.material.dialog-content-text :refer [dialog-content-text]]
   [reagent-mui.material.dialog-title :refer [dialog-title]]))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-orders [orders]
  (GET "/api/orders"
    {:headers (conj {"Accept" "application/transit+json"} (get-auth-header))
     :handler #(reset! orders (vec %))
     :error-handler error-handler}))

(defn delete-order [order]
  (DELETE (str "/api/orders/" order)
    {:headers (conj {"Accept" "application/transit+json"} (get-auth-header))
     :params {}
     :handler #(.log js/console "Deleted order: " order)
     :error-handler error-handler}))

(defn row-deletion-button
  "Button to delete selected items, opens a confirmation dialog"
  [dialog-open?]
  [:div
   [button {:variant :outlined
            :on-click #(reset! dialog-open? true)
            :start-icon (r/as-element [delete-icon])}
    "Delete Selected orders"]
   [dialog {:open @dialog-open?
            :on-close #(reset! dialog-open? false)}
    [dialog-title "Are you sure you want to delete these orders"]
    [dialog-content
     [dialog-content-text
      (str "Ids of orders which will be deleted: " @selected-orders)]
     [dialog-actions
      [button {:on-click #(reset! dialog-open? false)} "Go back"]
      [button {:start-icon (r/as-element [delete-forever-icon])
               :on-click #(do
                            (reset! dialog-open? false)
                            (doseq [order @selected-orders]
                              (delete-order order))
                            (get-orders orders))} "Delete Orders"]]]]])

(defn orders-datagrid-staff
  "Staff member version of datagrid component, allowing order deletion"
  []
  (let [deletion-dialog-open? (r/atom false)]
    (get-orders orders)
    [grid {:m 6
           :container true
           :spacing 1
           :justify-content "center"
           :align-items "center"
           :direction "column"}
     [grid {:item true}
      [typography {:variant :h4
                   :mb 2}
       "Orders List"]]
     [grid {:item true}
      [typography {:variant :body1
                   :mb 2}
       "Staff are able to add and delete orders"]]
     [grid {:item true
            :xs 4}
      [initialize-datagrid true]]
     [grid {:mt 1
            :container true
            :item true
            :justify-content "center"}
      [grid {:item true}
       [row-deletion-button deletion-dialog-open?]]]]))