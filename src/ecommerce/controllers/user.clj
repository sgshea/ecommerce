(ns ecommerce.controllers.user
  "API controller for user related data"
  (:require [clojure.tools.logging :as log]
            [ecommerce.model.users-model :as model]
            [ring.util.response :as r]))

(defn get-users
  "Render the list view with all the users in the addressbook."
  [req]
  (let [users (model/get-users (:db req))]
    (log/info "users:" users)
    (r/response users)))

(defn save
  [req]
  (-> req
      :params
      (select-keys [:id :first_name :last_name :email :role_id])
      (update :id #(some-> % not-empty Long/parseLong))
      (update :role_id #(some-> % not-empty Long/parseLong))
      (->> (reduce-kv (fn [m k v] (assoc! m (keyword "users" (name k)) v))
                      (transient {}))
           (persistent!)
           (model/save-user (:db req))))
  (r/status 200))

(defn delete-by-id [req]
  (log/info "deleting user: " (get-in req [:path-params :id]))
  (model/delete-user-by-id (:db req)
                           (get-in req [:path-params :id]))
  (r/status 200))
