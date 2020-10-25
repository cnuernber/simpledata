(ns simpledata.flights
  (:require [tech.v3.dataset :as ds]
            [tech.v3.dataset.column :as ds-col]
            [tech.v3.datatype.functional :as dfn]
            [tech.v3.datatype.argops :as argops]
            [tech.v3.datatype.unary-pred :as un-pred]
            [tech.v3.datatype :as dtype]
            [primitive-math :as pmath]
            [criterium.core :as crit]))

;;Optimization example


(defonce flights  (ds/->dataset "https://raw.githubusercontent.com/Rdatatable/data.table/master/vignettes/flights14.csv"))


(comment
  (time (->> (dfn/+ (flights "arr_delay")
                    (flights "dep_delay"))
             (argops/argfilter #(dfn/< % 0))
             (dtype/ecount)))

  (time  (->> (dfn/+ (flights "arr_delay")
                     (flights "dep_delay"))
              (filter #(pmath/< (long %) 0))
              (dtype/ecount)))

  (time  (->> (dfn/+ (flights "arr_delay")
                     (flights "dep_delay"))
              (argops/argfilter #(pmath/< (long %) 0))
              (dtype/ecount)))

  (time  (-> (dfn/+ (flights "arr_delay")
                    (flights "dep_delay"))
             (dfn/< 0)
             (un-pred/bool-reader->indexes)
             (dtype/ecount)))

  ;;Another way to get the same result is to use summation.  Booleans are
  ;;interpreted very specifically below where false is 0 and 1 is true.
  ;;Double summation is very fast.
  (time (-> (dfn/+ (flights "arr_delay")
                   (flights "dep_delay"))
            (dfn/< 0)
            (dfn/sum)))

  )
