(ns ecommerce.cljs.auth)

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