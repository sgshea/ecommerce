(ns ecommerce.cljs.login
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [ecommerce.cljs.auth :as auth]
   [reagent-mui.material.button :refer [button]]
   [reagent-mui.material.box :refer [box]]
   [reagent-mui.material.link :refer [link]]
   [reagent-mui.material.grid :refer [grid]]
   [reagent-mui.material.container :refer [container]]
   [reagent-mui.material.paper :refer [paper]]
   [reagent-mui.material.typography :refer [typography]]
   [reagent-mui.material.text-field :refer [text-field]]
   [reagent-mui.material.dialog :refer [dialog]]
   [reagent-mui.material.dialog-actions :refer [dialog-actions]]
   [reagent-mui.material.dialog-content :refer [dialog-content]]
   [reagent-mui.material.dialog-title :refer [dialog-title]]
   [reagent-mui.material.input-label :refer [input-label]]
   [reagent-mui.material.menu-item :refer [menu-item]]
   [reagent-mui.material.form-control :refer [form-control]]
   [reagent-mui.material.select :refer [select]]))

(def ^:private roles
  "Different roles of users"
  ["Customer" "Manager" "Staff"])

(defn event-value
  [e]
  (.. e -target -value))

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
  (let [user-sign-up (r/atom {:username ""
                              :email ""
                              :password ""})
        selected-role (r/atom 1)]
    (fn []
      [:div
       [dialog {:open @dialog-open?
                :on-close #(reset! dialog-open? false)}
        [dialog-title "Sign up new account"]
        [dialog-content
         [text-field {:auto-focus true
                      :margin :dense
                      :label "Username"
                      :on-change (fn [e]
                                   (swap! user-sign-up assoc-in [:username] (event-value e)))
                      :type :text
                      :full-width true
                      :variant :standard}]
         [text-field {:auto-focus false
                      :margin :dense
                      :label "Email"
                      :on-change (fn [e]
                                   (swap! user-sign-up assoc-in [:email] (event-value e)))
                      :type :email
                      :full-width true
                      :variant :standard}]
         [text-field {:auto-focus false
                      :margin :dense
                      :label "Password"
                      :on-change (fn [e]
                                   (swap! user-sign-up assoc-in [:password] (event-value e)))
                      :type :email
                      :full-width true
                      :variant :standard}]
         [role-select selected-role]
         [dialog-actions
          [button {:on-click #(reset! dialog-open? false)} "Close"]
          [button {:on-click #(do
                                (auth/register-user (into @user-sign-up {:role_id @selected-role}))
                                (reset! user-sign-up {:username ""
                                                      :email ""
                                                      :password ""})
                                (reset! selected-role 1)
                                (reset! dialog-open? false))} "Submit"]]]]])))

(defn login-form
  [sign-up-dialog-open?]
  (let [user (r/atom {:username ""
                      :password ""})]
    (fn []
      [box {:m 15
            :align-items :center}
       [typography {:variant :h5
                    :mb 1}
        "Sign in"]
       [text-field {:auto-focus true
                    :margin :dense
                    :label "Username"
                    :on-change (fn [e]
                                 (swap! user assoc-in [:username] (event-value e)))
                    :type :text
                    :full-width true
                    :variant :standard}]
       [text-field {:auto-focus false
                    :margin :dense
                    :label "Password"
                    :on-change (fn [e]
                                 (swap! user assoc-in [:password] (event-value e)))
                    :type :email
                    :full-width true
                    :variant :standard}]
       [button {:full-width true
                :variant :contained
                :sx {:mt 3
                     :mb 2}
                :on-click #((auth/login-user @user))}
        "Sign In"]
       [grid {:container true}
        [grid {:item true}
         [link {:component :button
                :variant :body1
                :on-click #(reset! sign-up-dialog-open? true)}
          "Sign up"]]]])))

(defn login-page
  "Main function for login/signup page"
  []
  (let [sign-up-dialog-open? (r/atom false)]
    [container {:max-width :xs}
     [user-dialog sign-up-dialog-open?]
     [box  {:mt 8
            :display :flex
            :flex-direction :column
            :align-items :center}
      [paper {:elevation 6}
       [login-form sign-up-dialog-open?]]]]))

;; -------------------------
;; Initialize app (login page)

(defn ^:dev/after-load mount-root []
  (d/render [login-page] (.getElementById js/document "login")))

(defn ^:export init! []
  (mount-root))