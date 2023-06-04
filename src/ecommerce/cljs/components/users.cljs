(ns ecommerce.cljs.components.users
  (:require 
   [reagent.core :as r]
   [ajax.core :refer [GET PUT DELETE]]
   [reagent-mui.material.button :refer [button]]
   [reagent-mui.x.data-grid :refer [data-grid]]
   [reagent-mui.util :refer [clj->js']]
   [reagent-mui.material.grid :refer [grid]]
   [reagent-mui.material.typography :refer [typography]]
   [reagent-mui.icons.delete :refer [delete] :rename {delete delete-icon}]
   [reagent-mui.icons.delete-forever :refer [delete-forever] :rename {delete-forever delete-forever-icon}]
   [reagent-mui.material.dialog :refer [dialog]]
   [reagent-mui.material.dialog-actions :refer [dialog-actions]]
   [reagent-mui.material.dialog-content :refer [dialog-content]]
   [reagent-mui.material.dialog-content-text :refer [dialog-content-text]]
   [reagent-mui.material.dialog-title :refer [dialog-title]]))
   
(def ^:private roles
  "Different roles of users"
  ["Customer" "Manager" "Staff"])

(defonce users (r/atom nil))

(defonce selected-ids (r/atom []))

(defn handler [response]
  (.log js/console (str response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-users [users]
  (GET "/api/users"
    {:headers {"Accept" "application/transit+json"}
     :handler #(reset! users (flatten %))
     :error-handler error-handler}))

(defn put-user [user]
  (PUT "/api/users"
    {:headers {"Accept" "application/transit+json"}
     :params user
     :handler handler
     :error-handler error-handler}))

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
                  :role (get roles (:users/role_id %)))
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
  [data-grid {:rows rows
              :columns col
              :auto-height true
              :initial-state (clj->js' {:pagination {:pagination-model {:page-size 5}}})
              :page-size-options [5]
              :checkbox-selection true
              :disable-row-selection-on-click true
              :density :standard
              :process-row-update row-update
              :on-process-row-update-error row-update-error
              :on-row-selection-model-change rows-selection-handler}])

(defn users-page
  "Main function defining users page"
  []
  (get-users users)
  (fn []
    (let [remove-user-dialog-open? (r/atom false)]
      [grid {:m 6
             :container true
             :spacing 1
             :justify-content "center"
             :align-items "center"
             :direction "column"}
       [grid {:item true}
        [typography {:variant :h4
                     :mb 2}
         "Users List"]]
       [grid {:item true}
        [data-grid-component (format-users-data @users) columns]]
       [grid {:mt 1
              :container true
              :item true
              :justify-content "center"}
        [grid {:item true}
         [row-deletion-button remove-user-dialog-open?]]]])))