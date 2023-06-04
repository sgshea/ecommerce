(ns ecommerce.cljs.auth
  (:require
   [reagent.core :as r]
   [ajax.core :refer [GET POST]]))

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
  [{:keys [token user]}]
  ;; set token into local storage and reset state with user
  (set-auth-token token)
  (reset! error-state nil)
  ;; redirect user to homepage for their role
  (case (:role_id user)
    0 (.assign (.-location js/window) "/home")
    1 (.assign (.-location js/window) "/staff")
    2 (.assign (.-location js/window) "/staff")
    (.assign (.-location js/window) "/login")
    ))

(defn auth-error!
  "Handler for errors"
  [{{:keys [error]} :response}]
  (reset! error-state error)
  (.log js/console error))

(defn set-auth-state
  "This uses token to get the user information, useful to use after changing page"
  []
  (GET "/api/me"
    {:headers (get-auth-header)
     :handler #(reset! auth-state (:user %))}))

(defn get-auth-state
  "This gets the user information from the atom"
  []
  @auth-state)

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

(defn logout 
  "Removes token, user-state, and redirects to login page"
  []
  (reset! auth-state nil)
  (reset! error-state nil)
  (set-auth-token "")
  (.assign (.-location js/window) "/login"))