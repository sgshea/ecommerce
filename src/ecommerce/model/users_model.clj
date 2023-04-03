(ns ecommerce.model.users-model
  "Model for user and user related data"
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

(def ^:private roles
  "Different roles of users"
  ["Management" "Logistics" "Support" "Development"])

(def ^:private initial-user-data
  "Create the database with this data."
  [{:first_name "Sammy" :last_name "Shea"
    :email "sammy@sammyshea.com" :role_id 4}
   {:first_name "John" :last_name "Smith"
    :email "john@smith.com" :role_id 1}])

(defn populate
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
                                first_name    varchar(32),
                                last_name     varchar(32),
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
               order by a.last_name, a.first_name"]))

(defn get-user-by-id
  "Returns a user given an id"
  [db id]
  (sql/get-by-id db :users id))

(defn get-roles
  "Returns all users under a certain role"
  [db]
  (sql/query db ["select * from role order by name"]))

(defn save-user
  "Attempts to save a user. If it exists, 
   it is an update, else saving a new user."
  [db user]
  (let [id (:addressbook/id user)]
    (if (and id (not (zero? id)))
      (sql/update! db :users
                   (dissoc user :users/id)
                   {:id id})
      (sql/insert! db :users
                   (dissoc user :users/id)))))

(defn delete-user-by-id
  "Deletes a user given an id"
  [db id]
  (sql/delete! db :users {:id id}))
