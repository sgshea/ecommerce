(ns ecommerce.cljs.staff-core
  "Defines pages for staff members and starts app"
  (:require
   [reagent.dom :as d]

   [ecommerce.cljs.common :refer [start-router! start-application]]
   [ecommerce.cljs.auth :refer [set-auth-state get-auth-state]]
   [ecommerce.cljs.components.home :as home]
   [ecommerce.cljs.components.users :as users]
   [ecommerce.cljs.components.orders-staff :refer [orders-datagrid-staff]]
   [ecommerce.cljs.components.products-staff :refer [products-datagrid-staff]]))

(def routes
  "Defines the routes for reitit"
  [["/staff"
    {:name ::home
     :view home/homepage}]

   ["/orders"
    {:name ::orders
     :view orders-datagrid-staff}]
   ["/products"
    {:name ::products
     :view products-datagrid-staff}]
   ["/users"
    {:name ::users
     :view users/users-page}]])

;; pages defined for the application components (ex: menu bar)
(def pages
  "Defines the pages to use in components"
  [{:name "Home"
    :link ::home}
   {:name "Orders"
    :link ::orders}
   {:name "Products"
    :link ::products}
   {:name "Users"
    :link ::users}])

;; -------------------------
;; Initialize app

(defn ^:dev/after-load mount-root []
  (d/render [start-application pages (:username (get-auth-state))] (.getElementById js/document "app")))

(defn ^:export init! []
  (start-router! routes)
  (set-auth-state)
  (mount-root))