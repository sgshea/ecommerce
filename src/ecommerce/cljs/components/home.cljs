(ns ecommerce.cljs.components.home
  "Component used to welcome user after logging in"
  (:require
   [ecommerce.cljs.auth :refer [get-auth-state]]
   [reagent-mui.material.grid :refer [grid]]
   [reagent-mui.material.typography :refer [typography]]))

(defn homepage
  "Homepage"
  []
  (let [auth-state (get-auth-state)
        role (case (js/parseInt (:role_id auth-state))
               1 "customer"
               2 "staff member"
               3 "manager"
               "unidentified")]
    [grid {:container true
           :justify-content "center"}
     [typography {:variant :h3}
      "Welcome back " role " " (:username auth-state)]]))
 