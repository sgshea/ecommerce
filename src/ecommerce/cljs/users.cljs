(ns ecommerce.cljs.users
  (:require 
   [reagent.core :as r]
   [ajax.core :refer [GET POST PUT]]
   [reagent-mui.material.button :refer [button]]
   [reagent-mui.x.data-grid :refer [data-grid]]
   [reagent-mui.util :refer [clj->js']]
   ))

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

(defn format-users-data [users]
  "Formats the user data from a GET request to rows for the data-table"
  (map #(hash-map :id (:users/id %)
                  :first-name (:users/first_name %)
                  :last-name (:users/last_name %)
                  :email (:users/email %)
                  :role (:role/name %))
       users))

(defn format-user-data-back [user]
  "Formats a single user back into the format for a POST or PUT request"
  (hash-map
   :id (:id user)
   :first_name (:first-name user)
   :last_name (:last-name user)
   :email (:email user)
   :role_id (case (:role user)
              "Management" 1
              "Logistics" 2
              "Support" 3
              "Development" 4)))

(defn row-update [new]
  (put-user (format-user-data-back (js->clj new :keywordize-keys true)))
  new)

(defn row-update-error [error]
  (.log js/console (str error)))

(def columns [{:field :id
               :headerName "ID"
               :width 80}
              {:field :first-name
               :headerName "First name"
               :width 130}
              {:field :last-name
               :headerName "Last name"
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
               }]])

(defn users-page []
  "Main function defining users page"
  (let [users (r/atom nil)]
    (get-users users)
    (fn []
      [:div
       [:h3 "Users List"]
       (data-grid-component (format-users-data @users) columns)])))