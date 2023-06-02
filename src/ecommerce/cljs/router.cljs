(ns ecommerce.cljs.router
  (:require
   [reagent.core :as r]
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.spec :as rss]))

(defonce page (r/atom nil))

(defn router-start!
  [routes]
  (rfe/start!
   (rf/router routes {:data {:coercion rss/coercion}})
   (fn [m] (reset! page m))
   {:use-fragment true}))
