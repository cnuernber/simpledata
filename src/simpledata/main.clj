(ns simpledata.main
  (:require [tech.v3.libs.arrow.in-place :as in-place]
            ;;need datafy defininitions
            [tech.v3.libs.arrow.schema]
            [tech.v3.datatype.functional :as dfn])
  (:gen-class))


(comment

  (do
    (defn- flights->arrow
      []
      (-> (ds/->dataset "flights14.csv")
          (arrow/write-dataset-to-stream! "flights14.arrow"))))
  )

(defn -main
  [& args]
  (System/load "/home/chrisn/dev/cnuernber/simpledata/ld-libs/liblarray.so")
  (let [flights (in-place/read-stream-dataset-inplace "flights14.arrow")]
    (println "Number of flights whose sum arrival and departure delay was less than zero:"
             (-> (dfn/+ (flights "arr_delay")
                        (flights "dep_delay"))
                 (dfn/< 0)
                 (dfn/sum)))
    0))
