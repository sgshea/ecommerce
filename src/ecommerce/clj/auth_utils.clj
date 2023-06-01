(ns ecommerce.clj.auth-utils
  (:require
   [buddy.auth :refer [authenticated?]]
   [buddy.auth.backends :as backends]
   [buddy.auth.middleware :refer [wrap-authentication]]
   [buddy.sign.jwt :as jwt]
   [ring.util.response :refer [redirect]]
   [clojure.tools.logging :as log]))

(def jwt-secret "JWT_SECRET")
(def backend (backends/jws {:secret jwt-secret}))

(defn wrap-jwt-authentication
  [handler]
  (wrap-authentication handler backend))

(defn auth-middleware
  "Checks that there is a valid authentication token"
  [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      [:status 401 :body {:error "Unauthorized"}])))

(defn return-user-id
  "Returns the user's id (gotten from the token of request)"
  [request]
  (:role_id (:identity request)))

(defn default-route
  "Redirects the route depending on the user"
  [handler]
  (fn [request]
    (let [user-role (return-user-id request)]
      (log/info "role!" user-role)
      (case user-role
        1 (redirect "/home")
        2 (redirect "/staff")
        3 (redirect "/staff")
        (redirect "/login")))))

(defn is-user
  "Checks that there is a valid authentication token and that the user's role is a user"
  [handler]
  (fn [request]
    (if (and
         (authenticated? request)
         (case (return-user-id request)
           1 true
           false))
      (handler request)
      [:status 401 :body {:error "Unauthorized"}])))

(defn is-staff
  "Checks that there is a valid authentication token and that the user's role is staff or manager"
  [handler]
  (fn [request]
    (if (and
         (authenticated? request)
           (case (return-user-id request)
             2 true
             3 true
             false))
      (handler request)
      [:status 401 :body {:error "Unauthorized"}])))

(defn create-token [payload]
  (jwt/sign payload jwt-secret))