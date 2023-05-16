(ns ecommerce.clj.model.users-model
  "Model for user and user related data"
  (:require [buddy.hashers :refer [check encrypt]]
            [clojure.tools.logging :as log]
            [honey.sql :as hsql]
            [honey.sql.helpers :as hh]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]))

(defn db-query-one [db sql]
  (jdbc/execute-one! db sql
                 {:return-keys true
                  :builder-fn rs/as-unqualified-maps}))

(def ^:private roles
  "Different roles of users"
  ["Customer" "Manager" "Staff"])

(def ^:private initial-user-data
  "Create the database with this data."
  [{:username "Sammy"
    :password "password"
    :email "sammy@sammyshea.com" :role_id 1}
   {:username "John"
    :password "1234"
    :email "john@smith.com" :role_id 2}])

(defn populate-users
  "Creates tables and auto-populates them with initial data"
  [db]
    (try
      (jdbc/execute-one! db
                         [(str "create table role (
                                id            integer primary key autoincrement,
                                name          varchar(32))")])
      (jdbc/execute-one! db
                         [(str "
                                create table users (
                                id            integer primary key autoincrement,
                                username      varchar(32),
                                password      varchar(32),
                                email         varchar(64),
                                role_id integer not null)")])
      (println "Created database and added user tables!")
      ;; if table creation was successful, it didn't exist before
      ;; so populate it...
      (try
        (doseq [r roles]
          (sql/insert! db :role {:name r}))
        (doseq [row initial-user-data]
          (sql/insert! db :users row))
        (println "Populated database with initial data!")
        (catch Exception e
          (println "Exception:" (ex-message e))
          (println "Unable to populate the initial data -- proceed with caution!")))
      (catch Exception e
        (println "Exception:" (ex-message e))
        (println "Looks like the database is already setup?"))))

(defn get-users
  "Return all available users, sorted by name.
   This is a join and keys will be namespace-qualified in table:
  users/id, role/name"
  [db]
  (sql/query db
             ["select a.*, r.name
               from users a
               join role r on a.role_id = r.id
               order by a.username"]))

(defn get-user-by-id
  "Returns a user given an id"
  [db id]
  (sql/get-by-id db :users id))

(defn get-roles
  "Returns all users under a certain role"
  [db]
  (sql/query db ["select * from role order by name"]))

(defn delete-user-by-id
  "Deletes a user given an id"
  [db id]
  (sql/delete! db :users {:id id}))

(defn create-user
  [db {:keys [username password email role_id]}]
  (let [hashed-password (encrypt password)
        created-user (->
                      (hh/insert-into :users)
                      (hh/columns :username :password :email :role_id)
                      (hh/values [[username hashed-password email role_id]])
                      hsql/format
                      (#(db-query-one db %)))
        sanitized-user (dissoc created-user :password)]
    (log/info "user" sanitized-user)
    sanitized-user))

(defn get-user-by-credientials
  [db {:keys [username password]}]
  (log/info "login" username password)
  (let [user (-> (hh/select :*)
                 (hh/from :users)
                 (hh/where := :username username)
                 hsql/format
                 (#(db-query-one db %)))
        sanitized-user (dissoc user :password)]
    (log/info "user" sanitized-user)
    (if (and user (check password (:password user)))
      sanitized-user
      nil)))

(defn get-user-by-payload
  [db {:keys [username]}]
  (let [user (-> (hh/select :*)
                 (hh/from :users)
                 (hh/where := :username username)
                 hsql/format
                 (#(db-query-one db %)))
        sanitized-user (dissoc user :password)]
      (if user
        sanitized-user
        nil)))