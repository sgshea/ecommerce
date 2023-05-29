(ns ecommerce.cljs.core
  (:require
   [reagent.dom :as d]
   [ecommerce.cljs.routes :refer [router-start! initialize]]))

;; -------------------------
;; Initialize app

(defn ^:dev/after-load mount-root []
  (d/render [initialize] (.getElementById js/document "app")))

(defn ^:export init! []
  (router-start!)
  (mount-root))