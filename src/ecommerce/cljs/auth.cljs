(ns ecommerce.cljs.auth
  (:require
   [reagent.core :as r]
   [ajax.core :refer [POST]]))

;; Auth state
(defonce auth-state (r/atom nil))
(defonce error-state (r/atom nil))

(defn set-auth-token
  "Saves authentication token into local storage"
  [auth-token]
  (.setItem js/localStorage
            "auth-user-token"
            auth-token))

(defn get-auth-token
  "Gets authentication token from local storage"
  []
  (.getItem js/localStorage "auth-user-token"))

(defn get-auth-header
  "Gets the auth token for use in a http request"
  []
  [:Authorization (str "Token " (get-auth-token))])

(defn auth-success!
  "Handler for success"
  [{{:keys [token] :as user} :user}]
  ;; set token into local storage
  (set-auth-token token)
  (reset! auth-state user)
  (reset! error-state nil)
  (.log js/console (clj->js user)))

(defn auth-error!
  "Handler for errors"
  [{{:keys [errors]} :response}]
  (reset! error-state errors)
  (.log js/console errors))

(defn login-user [user]
  (POST "/api/login"
    {:headers {"Accept" "application/transit+json"}
     :params user
     :handler auth-success!
     :error-handler auth-error!}))

(defn register-user [user]
  (POST "/api/register"
    {:headers {"Accept" "application/transit+json"}
     :params user
     :handler auth-success!
     :error-handler auth-error!}))