(ns ecommerce.cljs.users
  (:require 
   [reagent.core :as r]
   [ajax.core :refer [GET POST PUT DELETE]]
   [reagent-mui.material.button :refer [button]]
   [reagent-mui.x.data-grid :refer [data-grid]]
   [reagent-mui.util :refer [clj->js']]
   [reagent-mui.icons.add :refer [add] :rename {add add-icon}]
   [reagent-mui.icons.delete :refer [delete] :rename {delete delete-icon}]
   [reagent-mui.icons.delete-forever :refer [delete-forever] :rename {delete-forever delete-forever-icon}]
   [reagent-mui.material.box :refer [box]]
   [reagent-mui.material.text-field :refer [text-field]]
   [reagent-mui.material.dialog :refer [dialog]]
   [reagent-mui.material.dialog-actions :refer [dialog-actions]]
   [reagent-mui.material.dialog-content :refer [dialog-content]]
   [reagent-mui.material.dialog-content-text :refer [dialog-content-text]]
   [reagent-mui.material.dialog-title :refer [dialog-title]]
   [reagent-mui.material.input-label :refer [input-label]]
   [reagent-mui.material.menu-item :refer [menu-item]]
   [reagent-mui.material.form-control :refer [form-control]]
   [reagent-mui.material.select :refer [select]]))
   

(defonce users (r/atom nil))

(defonce selected-ids (r/atom []))

(defn event-value
  [e]
  (.. e -target -value))

(defn handler [response]
  (.log js/console (str response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-users [users]
  (GET "/api/users"
    {:headers {"Accept" "application/transit+json"}
     :handler #(reset! users (vec %))}))

(defn post-user [user]
  (POST "/api/users"
    {:headers {"Accept" "application/transit+json"}
     :params user
     :handler handler}))

(defn put-user [user]
  (PUT "/api/users"
    {:headers {"Accept" "application/transit+json"}
     :params user
     :handler handler}))

(defn delete-user [user]
  (DELETE (str "/api/users/" user)
    {:headers {"Accept" "application/transit+json"}
     :params {}
     :handler handler
     :error-handler error-handler}))

(defn format-users-data 
  "Formats the user data from a GET request to rows for the data-table"
  [users]
  (map #(hash-map :id (:users/id %)
                  :username (:users/username %)
                  :password (:users/password %)
                  :email (:users/email %)
                  :role (:role/name %))
       users))

(defn format-user-data-back 
  "Formats a single user back into the format for a POST or PUT request"
  [user]
  (hash-map
   :id (:id user)
   :username (:username user)
   :password (:password user)
   :email (:email user)
   :role_id (case (:role user)
              "Customer" 1
              "Manager" 2
              "Staff" 3)))

(defn rows-selection-handler
  "Updates the selected-ids atom with the ids of selected rows"
  [selection-model]
  (reset! selected-ids selection-model)
  (.log js/console (str "Selected ids: " @selected-ids)))

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
    [dialog-title "Are you sure you want to delete these users?"]
    [dialog-content
     [dialog-content-text
      (str "Ids of users which will be deleted: " @selected-ids)]
     [dialog-actions
      [button {:on-click #(reset! dialog-open? false)} "Go back"]
      [button {:start-icon (r/as-element [delete-forever-icon])
               :on-click #(do
                            (reset! dialog-open? false)
                            (doseq [user @selected-ids]
                              (delete-user user))
                            (get-users users))} "Delete Users"]]]]])

(defn row-update
  "Updates a row, used for the datagrid"
  [new]
  (put-user (format-user-data-back (js->clj new :keywordize-keys true)))
  (get-users users)
  new)

(defn row-update-error [error]
  (.log js/console (str error)))

(defn role-select
  "Select component for roles"
  [selected-role]
    [box {:component :form
          :sx {:mt 5}}
     [form-control
      [input-label {:id "select-roles"}
       "Role"]
      [select {:native false
               :value @selected-role
               :on-change (fn [e] (reset! selected-role (event-value e)))
               :label "Role"
               :id "select-roles"
               :label-id "select-roles"}
       [menu-item {:value 1}
        "Customer"]
       [menu-item {:value 2}
        "Manager"]
       [menu-item {:value 3}
        "Staff"]]]])

(defn user-dialog
  "Form dialog to add a new user"
  [dialog-open?]
  (let [user (r/atom {:username ""
                      :email ""})
        selected-role (r/atom 1)]
    [:div
     [button {:variant :outlined
              :on-click #(reset! dialog-open? true)
              :start-icon (r/as-element [add-icon])}
      "Add New User"]
     [dialog {:open @dialog-open?
              :on-close #(reset! dialog-open? false)}
      [dialog-title "Add New User"]
      [dialog-content
       [dialog-content-text
        "To add a new user, please enter a name, email, and role"]
       [text-field {:auto-focus true
                    :margin :dense
                    :id :first-name-field
                    :label "Username"
                    :on-change (fn [e]
                                 (swap! user assoc-in [:username] (event-value e)))
                    :type :text
                    :full-width true
                    :variant :standard}]
       [text-field {:auto-focus false
                    :margin :dense
                    :id :email-field
                    :label "Email"
                    :on-change (fn [e]
                                 (swap! user assoc-in [:email] (event-value e)))
                    :type :email
                    :full-width true
                    :variant :standard}]
       [role-select selected-role]
       [dialog-actions
        [button {:on-click #(reset! dialog-open? false)} "Close"]
        [button {:on-click #(do
                              (post-user (merge @user {:role_id @selected-role}))
                              (reset! user {:username ""
                                            :email ""})
                              (reset! dialog-open? false)
                              (get-users users))} "Submit"]]]]]))

(def columns [{:field :id
               :headerName "ID"
               :width 80}
              {:field :username
               :headerName "Username"
               :width 130}
              {:field :email
               :headerName "Email"
               :width 200
               :editable true}
              {:field :role
               :headerName "Role"
               :width 150
               :editable true}])

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

(defn users-page 
  "Main function defining users page"
  []
  (let [add-user-dialog (r/atom false)
        remove-user-dialog (r/atom false)]
    (get-users users)
    (fn []
      [:div
       [:h3 "Users List"]
       [data-grid-component (format-users-data @users) columns]
       [user-dialog add-user-dialog]
       [row-deletion-button remove-user-dialog]])))