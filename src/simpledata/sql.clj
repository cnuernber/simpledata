(ns simpledata.sql
  (:require [tech.ml.dataset.sql :as ds-sql]
            [tech.ml.dataset.sql.impl :as ds-sql-impl]
            [next.jdbc :as jdbc])
  (:import [java.sql Connection]))


(defonce dev-conn* (delay (doto (-> (ds-sql-impl/jdbc-postgre-connect-str
                                     "localhost:5432" "dev-user"
                                     "dev-user" "unsafe-bad-password")
                                    (jdbc/get-connection {:auto-commit false}))
                            (.setCatalog "dev-user"))))


(defn insert-dataset!
  ([conn ds]
   (when-not (ds-sql/table-exists? conn ds)
     (ds-sql/create-table! conn ds))
   (ds-sql/insert-dataset! conn ds))
  ([ds]
   (insert-dataset! @dev-conn* ds)))


(defn sql->dataset
  ([conn sql]
   (ds-sql/sql->dataset conn sql))
  ([sql]
   (sql->dataset @dev-conn* sql)))


(defn table->dataset
  ([conn table-name]
   (sql->dataset conn (format "SELECT * FROM %s" table-name)))
  ([table-name]
   (table->dataset @dev-conn* table-name)))


(defn tables
  ([^Connection conn]
   (-> (.getMetaData conn)
       (.getTables nil nil "%" (into-array String ["TABLE"]))
       (ds-sql/result-set->dataset)))
  ([] (tables @dev-conn*)))
