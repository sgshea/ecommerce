(ns ecommerce.cljs.common
  (:require
   [reagent.core :as r]
   [ecommerce.cljs.router :as router]
   [reagent-mui.material.css-baseline :refer [css-baseline]]

   [ecommerce.cljs.components.menu-bar :as menu-bar]
   [reagent-mui.colors :as colors]
   [reagent-mui.styles :as styles]
   ))

;; Common functionality between staff and user pages
;; Abstracts routing and initialization

;; Set dark as default
(defonce theme-mode
  (r/atom :dark)) 

(defn custom-theme [mode] {:palette {:mode mode
                             :primary colors/blue 
                             :secondary colors/red}})

;; -------------------------
;; Initialize website

(defn start-application 
  "Initialize application"
  [pages]
   [:<>
    [styles/theme-provider (styles/create-theme (custom-theme @theme-mode))
     [css-baseline
      [menu-bar/menu-bar theme-mode pages]
      [:div
       (if @router/page
         (let [view (:view (:data @router/page))]
           [view @router/page])
         nil)]]]])

(defn start-router!
  [routes]
  (router/router-start! routes))