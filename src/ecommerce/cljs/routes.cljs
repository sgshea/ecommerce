(ns ecommerce.cljs.routes
  (:require
   [reagent.core :as r]
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.spec :as rss]
   [reagent-mui.material.css-baseline :refer [css-baseline]]

   [ecommerce.cljs.components.menu-bar :as menu-bar]
   [ecommerce.cljs.users :as users]
   [ecommerce.cljs.products :as products]
   [ecommerce.cljs.user-login :as login-page]

   [reagent-mui.colors :as colors]
   [reagent-mui.styles :as styles]))

;; Set dark as default
(defonce theme-mode
  (r/atom :dark)) 

(defn custom-theme [mode] {:palette {:mode mode
                             :primary colors/blue 
                             :secondary colors/red}})

(def pages
  [{:name "Home"
    :link ::home}
   {:name "Login"
    :link ::login}
   {:name "Users"
    :link ::users}
   {:name "Products"
    :link ::products}])

;; -------------------------
;; Initialize website

(defn home []
  [:div "CLJS!"])
   
(defonce match (r/atom nil))

(defn initialize 
  "Initialize pages using routes"
  []
   [:<>
    [styles/theme-provider (styles/create-theme (custom-theme @theme-mode))
     [css-baseline
      [menu-bar/menu-bar theme-mode pages]
      [:div
       (if @match
         (let [view (:view (:data @match))]
           [view @match])
         nil)]]]])

(def routes
  [["/"
    {:name ::home
     :view home}]

   ["/login"
    {:name ::login
     :view login-page/login-page}]
   ["/users"
    {:name ::users
     :view users/users-page}]
   ["/products"
    {:name ::products
     :view products/products-page}]])

(defn router-start!
  []
  (rfe/start!
   (rf/router routes {:data {:coercion rss/coercion}})
   (fn [m] (reset! match m))
   {:use-fragment false}))
