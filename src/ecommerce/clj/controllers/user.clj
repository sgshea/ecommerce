(ns ecommerce.clj.controllers.user
  "API controller for user related data"
  (:require
   [ecommerce.clj.model.users-model :as model]
   [ecommerce.clj.auth-utils :refer [create-token]]
   [ring.util.response :as r]))

(defn get-users
  "Gets all users"
  [req]
  {:status 200
   :body (model/get-users (:db req))})

(defn register
  "Creates a new user and returns the user with token"
  [{:keys [db parameters]}]
  (let [data (:body parameters)
        user (model/create-user db data)]
    {:status 201
     :body {:user user
            :token (create-token user)}}))

(defn login
  "Login a user, returning their token"
  [{:keys [db parameters]}]
  (let [data (:body parameters)
        user (model/get-user-by-credientials db data)]
    (if (nil? user)
      {:status 404
       :body {:error "Invalid credientials"}}
      {:status 200
       :body {:user user
              :token (create-token user)}})))

(defn me
  "Gets the current user token"
  [req]
  (let [payload (:identity req)
        user (model/get-user-by-payload (:db req) payload)]
    (if (nil? user)
      {:status 404
       :body {:error "Unauthorized"}}
      {:status 200
       :body {:user user
              :token (create-token user)}})))

;; (defn edit
;;   [req]
;;   (log/info "editing user: " (:body (:parameters req)))
;;   (model/save-user (:db req)
;;                    (get-in req [:parameters :body]))
;;   (r/status 200))

(defn delete-by-id 
  "Deletes a specific user given id"
  [{:keys [db path-params]}]
  (model/delete-user-by-id db
                           (:id path-params))
  (r/status 200))