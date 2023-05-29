(ns ecommerce.cljs.core
  (:require
   [reagent.dom :as d]
   [ecommerce.cljs.routes :as routes]))

;; -------------------------
;; Initialize app

(defn ^:dev/after-load mount-root []
  (d/render [routes/initialize] (.getElementById js/document "app")))

(defn ^:export init! []
  (routes/router-start!)
  (mount-root))