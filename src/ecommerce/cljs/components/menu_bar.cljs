(ns ecommerce.cljs.components.menu-bar
  (:require
   [reagent.core :as r]
   [reitit.frontend.easy :as rfe]
   [ecommerce.cljs.auth :refer [logout]]
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
   [reagent-mui.material.icon-button :refer [icon-button]]
   [reagent-mui.material.avatar :refer [avatar]]))

(defonce anchor-nav (r/atom nil))
(defonce anchor-avatar (r/atom nil))

(defn pages-button
  "Individual menu button for each page"
  [page]
  [menu-item {:on-click #(reset! anchor-nav nil)
              :key (page :name)
              :component :a
              :href (rfe/href (page :link))}
   (page :name)])

(defn menu-bar
  "Top bar for the pages"
  [theme-mode pages username]
  [app-bar {:enable-color-on-dark true
            :color "transparent"
            :position :sticky}
   [container {:max-width "x1"}
    [toolbar {:disable-gutters true}
     [typography {:variant :h5
                  :sx {:mr 2
                       :display {:xs "none"
                                 :md "flex"}
                       :fontFamily "monospace"
                       :color "text.primary"}}
      "Ecommerce Web App"]
     [box {:sx {:flex-grow 1
                :display {:md "none"}}}
      [icon-button {:id "menu-icon"
                    :size "large"
                    :on-click (fn [e]
                                (reset! anchor-nav (.. e -target)))
                    :color "inherit"}
       [menu-icon]]
      [menu {:id "menu-appbar"
             :anchor-el @anchor-nav
             :keep-mounted true
             :open (some? @anchor-nav)
             :onClose #(reset! anchor-nav nil)}
       (map pages-button pages)]]
     [typography {:variant :h5
                  :noWrap true
                  :sx {:mr 2
                       :display {:xs "flex"
                                 :md "none"}
                       :fontFamily "monospace"
                       :color "text.primary"}}
      "Ecommerce Web App"]
     [box {:sx {:flex-grow 1
                :display {:xs "none"
                          :md "flex"}}}
      (map pages-button pages)]
     [box {:sx {:display {:xs "flex"
                          :md "flex"}}}
      [icon-button {:sx {:m 1}
                    :color "inherit"
                    :on-click #(if (= @theme-mode :light)
                                  (reset! theme-mode :dark)
                                  (reset! theme-mode :light))}
       (if (= @theme-mode :light)
         [brightness-4]
         [brightness-7])]
      [icon-button {:sx {:m 1}
                    :color "inherit"
                    :on-click (fn [e]
                                (reset! anchor-avatar (.. e -target)))}
       [avatar {:sx {:bgcolor "#2196f3"}}
        (first username)]]
      [menu {:id "avatar-appbar"
             :anchor-el @anchor-avatar
             :keep-mounted true
             :open (some? @anchor-avatar)
             :onClose #(reset! anchor-avatar nil)}
       [menu-item {:on-click logout}
        "Logout"]]]]]])