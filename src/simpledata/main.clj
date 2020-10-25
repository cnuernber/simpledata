(ns simpledata.main
  (:require [tech.v3.dataset :as ds]
            [tech.v3.datatype.functional :as dfn])
  (:gen-class))



(defn -main
  [& args]
  (let [flights
        (ds/->dataset "flights14.csv")]
    (println "Number of flights whose average arrival and departure delay was less than zero:"
             (-> (dfn/+ (flights "arr_delay")
                        (flights "dep_delay"))
                 (dfn/< 0)
                 (dfn/sum)))
    0))
