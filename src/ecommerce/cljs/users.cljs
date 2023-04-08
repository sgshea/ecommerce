(ns ecommerce.cljs.users
  (:require 
   [reagent.core :as r]
   [ajax.core :refer [GET POST]]
   [reagent-mui.x.data-grid :refer [data-grid]]
   [reagent-mui.util :refer [clj->js']]))

(defn handler [response]
  (.log js/console (str response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-users [users]
  (GET "/api/users"
    {:headers {"Accept" "application/transit+json"}
     :handler #(reset! users (vec %))}))

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
               :width 200}
              {:field :role
               :headerName "Role"
               :width 150}])

(defonce selected (r/atom nil))

(defn data-grid-component [rows col]
  [:div {:style {:height 400 :width 800}}
   [data-grid {:rows rows
               :columns col
               :initial-state (clj->js' {:pagination {:pagination-model {:page-size 5}}})
               :page-size-options [5]
               :checkbox-selection true
               :disable-row-selection-on-click true
               :density :standard
               }]])

(defn format-users-data [users]
  "Formats the user data from a GET request to rows for the data-table"
  (map #(hash-map :id (:users/id %)
                  :first-name (:users/first_name %)
                  :last-name (:users/last_name %)
                  :email (:users/email %)
                  :role (:role/name %))
       users))

(defn users-page []
  "Main function defining users page"
  (let [users (r/atom nil)]
    (get-users users)
    (fn []
      [:div
       [:h3 "Users List"]
       (data-grid-component (format-users-data @users) columns)])))