(ns simpledata.main
  (:require ;; [tech.v3.libs.arrow.in-place :as in-place]
            ;;need datafy defininitions
            ;; [tech.v3.libs.arrow.schema]
   [tech.v3.dataset :as ds]
   [tech.v3.io.url :as url]
   [clojure.java.io :as clj-io]
            ;; [tech.v3.datatype.functional :as dfn]
   )
  (:import [java.net URL])
  (:gen-class))


(set! *warn-on-reflection* true)

(comment

  (do
    (defn- flights->arrow
      []
      (-> (ds/->dataset "flights14.csv")
          (arrow/write-dataset-to-stream! "flights14.arrow"))))

  (defn- flights->nippy
    []
    (-> (ds/->dataset "flights14.csv")
        (ds/write! "flights14.nippy")))
  )

(def test-url "https://raw.githubusercontent.com/Rdatatable/data.table/master/vignettes/flights14.csv")

(defn -main
  [& args]
  (println (ds/->dataset test-url))
  0)
