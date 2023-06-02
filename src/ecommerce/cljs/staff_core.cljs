(ns ecommerce.cljs.staff-core
  (:require
   [reagent.dom :as d]

   [ecommerce.cljs.users :as users]
   [ecommerce.cljs.products :as products]

   [ecommerce.cljs.common :refer [start-router! start-application]]))

(defn home []
  [:div "Staff home"])

(def routes
  [["/"
    {:name ::home
     :view home}]

   ["/users"
    {:name ::users
     :view users/users-page}]
   ["/products"
    {:name ::products
     :view products/products-page}]])

;; pages defined for the application components (ex: menu bar)
(def pages
  [{:name "Home"
    :link ::home}
   {:name "Users"
    :link ::users}
   {:name "Products"
    :link ::products}])

;; -------------------------
;; Initialize app

(defn ^:dev/after-load mount-root []
  (d/render [start-application pages] (.getElementById js/document "app")))

(defn ^:export init! []
  (start-router! routes)
  (mount-root))