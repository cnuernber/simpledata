(ns simpledata.boulder-crime
  (:require [tech.ml.dataset :as ds]
            [tech.v2.datatype.functional :as dfn]
            [tech.viz.vega :as vega]
            [simpledata.sql :as sql]
            [simpledata.util :as util]))


(defn obtain-dataset
  []
  (-> (util/cache-remote->local-file
       "https://opendata.arcgis.com/datasets/fa19e19360e74c15a5ebe8b65cf523ad_0.csv"
       "file://download/boulder-crime.csv")
      (ds/->dataset {:key-fn keyword
                     :dataset-name "boulder_crime"})))


(def ds* (delay (obtain-dataset)))


(comment
  ;;Example datetime string
  ;;2020/02/28 07:00:00+00

  ;;Parsing the reportdate could have been done two ways:

  ;;First, just fix the dataset after parsing.  Column-cast is smart enough
  ;;to call the parsing system on string columns.
  (def ds (ds/column-cast @ds* :REPORTDATE [:zoned-date-time
                                            "yyyy/MM/dd HH:mm:ssx"]))


  ;;Second, reload the dataset
  (def ds (do
            ;;make sure file is downloaded
            @ds*
            (ds/->dataset "file://data/boulder-crime.csv"
                          {:key-fn keyword
                           :parser-fn {"REPORTDATE"
                                         [:zoned-date-time
                                          "yyyy/MM/dd HH:mm:ssx"]}})))

  (vary-meta (ds/descriptive-stats ds)
             assoc :print-column-max-width 15)


  (->> (ds :OFFENSE)
       (frequencies)
       (sort-by second >))


  (sql/insert-dataset! (vary-meta ds assoc :name "boulder_crime2"))

  )
