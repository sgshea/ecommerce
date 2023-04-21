(ns ecommerce.cljs.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.spec :as rss]
   [fipp.edn :as fedn]
   [ecommerce.cljs.users :as users]
   [reagent-mui.colors :as colors]
   [reagent-mui.styles :as styles]
   [reagent-mui.material.css-baseline :refer [css-baseline]]
   [reagent-mui.material.typography :refer [typography]]
   [reagent-mui.material.box :refer [box]]
   [reagent-mui.material.container :refer [container]]
   [reagent-mui.material.app-bar :refer [app-bar]]
   [reagent-mui.material.toolbar :refer [toolbar]]
   [reagent-mui.material.button :refer [button]]
   [reagent-mui.icons.brightness-4 :refer [brightness-4]]
   [reagent-mui.icons.brightness-7 :refer [brightness-7]]
   [reagent-mui.material.menu :refer [menu] :as menu-component]
   [reagent-mui.icons.menu :refer [menu] :rename {menu menu-icon}]
   [reagent-mui.material.icon-button :refer [icon-button]]))



;; Set dark as default
(defonce theme-mode
  (r/atom :dark)) 

(defn toggle-dark-mode
  "Toggles between light and dark mode"
  []
  (if (= @theme-mode :light)
    (reset! theme-mode :dark)
    (reset! theme-mode :light)))

(defn custom-theme [mode] {:palette {:mode mode
                             :primary colors/blue 
                             :secondary colors/red}})
;; -------------------------
;; App Bar
(defn event-value
  [e]
  (.. e -target -value))

;; Below is the code for the app-bar
(def pages
  [{:name "Home"
    :link ::home}
   {:name "Users"
    :link ::users}])

(defonce anchorElNav (r/atom false))

(defn pages-button
  [page]
  [button {:key (page :name)
           :on-click #(reset! anchorElNav false)
           :sx {:m 2
                :color "text.secondary"
                :display :block}
           :href (rfe/href (page :link))}
   (page :name)])

(defn menu-bar
  "Top bar for the pages"
  []
  [app-bar {:enableColorOnDark true
            :color "transparent"
            :position :sticky}
   [container {:maxWidth "x1"}
     [toolbar
      [typography {:variant :h5
                   :sx {:mr 2
                        :display {:xs "none"
                                  :md "flex"}
                        :fontFamily "monospace"
                        :color "text.primary"}}
       "Ecommerce Web App"]
      [box {:sx {:flexGrow 1
                 :display {:md "none"}}}
       [icon-button {:size "large"
                     :aria-label ""
                     :aria-controls "menu-appbar"
                     :aria-haspopup true
                     :on-click #(reset! anchorElNav true)
                     :color "inherit"}
        [menu-icon]]
       [menu {:id "menu-appbar" 
              :anchor-reference :none
              :anchor-origin {:vertical "top"
                             :horizontal "left"}
              :keepMounted true
              :open @anchorElNav
              :onClose #(reset! anchorElNav false)}
        (map pages-button pages)]]
      [typography {:variant :h5
                   :noWrap true
                   :sx {:mr 2
                        :display {:xs "flex"
                                  :md "none"}
                        :fontFamily "monospace"
                        :color "text.primary"}}
       "Ecommerce Web App"]
      [box {:sx {:flexGrow 1
                 :display {:xs "none"
                           :md "flex"}}}
       (map pages-button pages)]
      [icon-button {:sx {:m 1}
                    :color "inherit"
                    :on-click #(toggle-dark-mode)}
       (if (= @theme-mode :light)
         [brightness-4]
         [brightness-7])]
      ]]])

;; -------------------------
;; Initialize website
   
(defonce match (r/atom nil))

(defn current-page []
   [:<>
    [styles/theme-provider (styles/create-theme (custom-theme @theme-mode))
     [css-baseline
      [menu-bar]
      [:div
        (if @match
          (let [view (:view (:data @match))]
            [view @match])
          nil)
        [:pre (with-out-str (fedn/pprint @match))]]]
      ]])

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