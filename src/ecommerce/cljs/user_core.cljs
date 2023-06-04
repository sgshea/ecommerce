(ns ecommerce.cljs.user-core
  "Defines pages for users and starts app"
  (:require
   [reagent.dom :as d]

   [ecommerce.cljs.common :refer [start-router! start-application]]
   [ecommerce.cljs.auth :refer [set-auth-state get-auth-state]]
   [ecommerce.cljs.products :as products]
   [ecommerce.cljs.components.home :as home]))

(def routes
  "Defines the routes for reitit"
  [["/home"
    {:name ::home
     :view home/homepage}]

   ["/products"
    {:name ::products
     :view products/products-page}]])

;; pages defined for the application components (ex: menu bar)
(def pages
  "Defines the pages to use in components"
  [{:name "Home"
    :link ::home}
   {:name "Products"
    :link ::products}])

;; -------------------------
;; Initialize app

(defn ^:dev/after-load mount-root []
  (d/render [start-application pages (:username (get-auth-state))] (.getElementById js/document "app")))

(defn ^:export init! []
  (start-router! routes)
  (set-auth-state)
  (mount-root))