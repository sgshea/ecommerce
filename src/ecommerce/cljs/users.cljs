(ns ecommerce.cljs.users
  (:require 
   [reagent.core :as r]
   [ajax.core :refer [GET POST]]))

(defn handler [response]
  (.log js/console (str response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn user-list [users]
  [:ul
   (for [{:keys [users/first_name users/last_name users/email]} @users]
     [:li
      [:p first_name " " last_name]
      [:p email]])])

(defn get-users [users]
  (GET "/users"
    {:headers {"Accept" "application/transit+json"}
     :handler #(reset! users (vec %))}))

(defn users-page []
  (let [users (r/atom nil)]
    (get-users users)
    (fn []
      [:div
       [:h3 "Users"]
       [:div
        [user-list users]]])))