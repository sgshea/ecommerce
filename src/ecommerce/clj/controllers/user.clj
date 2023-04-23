(ns ecommerce.clj.controllers.user
  "API controller for user related data"
  (:require [clojure.tools.logging :as log]
            [ecommerce.clj.model.users-model :as model]
            [ring.util.response :as r]))

(defn get-users
  "Render the list view with all the users in the addressbook."
  [req]
  (let [users (model/get-users (:db req))]
    (log/info "users:" users)
    (r/response users)))

(defn save-new
  [req]
  (log/info "adding user: " (:body (:parameters req)))
  (model/save-user (:db req)
                   (get-in req [:parameters :body]))
  (r/status 200))

(defn edit
  [req]
  (log/info "editing user: " (:body (:parameters req)))
  (model/save-user (:db req)
                   (get-in req [:parameters :body]))
  (r/status 200))

(defn delete-by-id [req]
  (log/info "deleting user: " (get-in req [:path-params :id]))
  (model/delete-user-by-id (:db req)
                           (get-in req [:path-params :id]))
  (r/status 200))
