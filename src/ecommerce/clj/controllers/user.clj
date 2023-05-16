(ns ecommerce.clj.controllers.user
  "API controller for user related data"
  (:require [clojure.tools.logging :as log]
            [ecommerce.clj.model.users-model :as model]
            [ecommerce.clj.auth-utils :refer [create-token]]
            [ring.util.response :as r]))

(defn get-users
  "Render the list view with all the users"
  [req]
  (let [users (model/get-users (:db req))]
    (r/response users)))

(defn register
  [{:keys [db parameters]}]
  (let [data (:body parameters)
        user (model/create-user db data)]
    {:status 201
     :body {:user user
            :token (create-token user)}}))

(defn login
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
  "Gets the current user"
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

(defn delete-by-id [req]
  (log/info "deleting user: " (get-in req [:path-params :id]))
  (model/delete-user-by-id (:db req)
                           (get-in req [:path-params :id]))
  (r/status 200))