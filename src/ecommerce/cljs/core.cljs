(ns ecommerce.cljs.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.spec :as rss]
   [fipp.edn :as fedn]
   [ecommerce.cljs.users :as users]))

(defonce match (r/atom nil))

(defn current-page []
  [:div
   [:ul
    [:li [:a {:href (rfe/href ::home)} "Homepage"]]
    [:li [:a {:href (rfe/href ::users)} "Users"]]]
   (if @match
     (let [view (:view (:data @match))]
       [view @match])
     nil)
   [:pre (with-out-str (fedn/pprint @match))]])

(defn home []
  [:div "CLJS!"])

(def routes
  [["/"
    {:name ::home
     :view home}]

   ["/users"
    {:name ::users
     :view users/users-page}]])


;; -------------------------
;; Initialize app

(defn ^:dev/after-load mount-root []
  (rfe/start!
   (rf/router routes {:data {:coercion rss/coercion}})
   (fn [m] (reset! match m))
   {:use-fragment true})
  (d/render [current-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))