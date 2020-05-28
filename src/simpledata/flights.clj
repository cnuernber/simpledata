(ns simpledata.flights
  (:require [tech.ml.dataset :as ds]
            [tech.ml.dataset.column :as ds-col]
            [tech.v2.datatype.functional :as dfn]
            [tech.v2.datatype :as dtype]
            [tech.v2.datatype.typecast :as typecast]
            [tech.v2.datatype.boolean-op :as boolean-op]
            [primitive-math :as pmath]
            [criterium.core :as crit]))

;;Optimization example


(defonce flights  (ds/->dataset "https://raw.githubusercontent.com/Rdatatable/data.table/master/vignettes/flights14.csv"))


(comment
  (crit/quick-bench  (->> (dfn/+ (flights "arr_delay")
                                 (flights "dep_delay"))
                          (dfn/argfilter #(dfn/< % 0))
                          (dtype/ecount)))
  ;;90ms - dfn functions pay a high cost for dispatch meaning they aren't to be used
  ;;in tight loops.

  (crit/quick-bench  (->> (dfn/+ (flights "arr_delay")
                                 (flights "dep_delay"))
                          (filter #(pmath/< (long %) 0))
                          (dtype/ecount)))
  ;;16ms

  (crit/quick-bench  (->> (dfn/+ (flights "arr_delay")
                                 (flights "dep_delay"))
                          (dfn/argfilter #(pmath/< (long %) 0))
                          (dtype/ecount)))
  ;;2.5ms

  (crit/quick-bench  (->> (dfn/+ (flights "arr_delay")
                                 (flights "dep_delay"))
                          (dfn/argfilter (fn [^long data]
                                           (pmath/< data 0)))
                          (dtype/ecount)))
  ;;2.2ms

  (crit/quick-bench
   (boolean-op/bool-reader-indexes->bitmap
    {}
    (let [arr-delay (typecast/datatype->reader :int16 (flights "arr_delay"))
          dep-delay (typecast/datatype->reader :int16 (flights "dep_delay"))]
      (dtype/make-reader :boolean
                         (dtype/ecount arr-delay)
                         (pmath/< (pmath/+ (.read arr-delay idx)
                                           (.read dep-delay idx))
                                  0)))))
  ;;1.44ms

  )
