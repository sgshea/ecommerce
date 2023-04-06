(ns ecommerce.cljs.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]))


(defn home []
  [:div "CLJS!"])

;; -------------------------
;; Initialize app

(defn ^:dev/after-load mount-root []
  (d/render [home] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))