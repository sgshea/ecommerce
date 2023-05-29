(ns ecommerce.cljs.components.menu-bar
  (:require
   [reagent.core :as r]
   [reitit.frontend.easy :as rfe]
   [reagent-mui.material.typography :refer [typography]]
   [reagent-mui.material.box :refer [box]]
   [reagent-mui.material.container :refer [container]]
   [reagent-mui.material.app-bar :refer [app-bar]]
   [reagent-mui.material.toolbar :refer [toolbar]]
   [reagent-mui.icons.brightness-4 :refer [brightness-4]]
   [reagent-mui.icons.brightness-7 :refer [brightness-7]]
   [reagent-mui.material.menu :refer [menu] :as menu-component]
   [reagent-mui.material.menu-item :refer [menu-item]]
   [reagent-mui.icons.menu :refer [menu] :rename {menu menu-icon}]
   [reagent-mui.material.icon-button :refer [icon-button]]))

(defonce pages
  [{:name "Home"
    :link ::home}
   {:name "Login"
    :link ::login}
   {:name "Users"
    :link ::users}
   {:name "Products"
    :link ::products}])

(defonce anchorElNav (r/atom false))

(defn pages-button
  [page] 
   [menu-item {:on-click #(reset! anchorElNav false)
               :key (page :name)
               :component :a
               :href (rfe/href (page :link))}
    (page :name)])

(defn menu-bar
  "Top bar for the pages"
  [theme-mode]
  [app-bar {:enableColorOnDark true
            :color "transparent"
            :position :sticky}
   [container {:maxWidth "x1"}
     [toolbar {:disable-gutters true}
      [typography {:variant :h5
                   :sx {:mr 2
                        :display {:xs "none"
                                  :md "flex"}
                        :fontFamily "monospace"
                        :color "text.primary"}}
       "Ecommerce Web App"]
      [box {:sx {:flexGrow 1
                 :display {:md "none"}}}
       [icon-button {:id "menu-icon"
                     :size "large"
                     :on-click #(reset! anchorElNav true)
                     :color "inherit"}
        [menu-icon]]
       [menu {:id "menu-appbar"
              :anchor-position {:top 50
                                :left 0}
              :anchor-reference :anchorPosition
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
                    :on-click #((if (= @theme-mode :light)
                                  (reset! theme-mode :dark)
                                  (reset! theme-mode :light)))}
       (if (= @theme-mode :light)
         [brightness-4]
         [brightness-7])]
      ]]])