(ns simpledata.airports
  (:require [tech.ml.dataset :as ds]
            [tech.v2.datatype.functional :as dfn]))


(defn obtain-dataset
  []
  (-> (ds/->dataset "https://raw.githubusercontent.com/jpatokal/openflights/master/data/airports.dat"
                    {:header-row? false})
      (ds/rename-columns {0 :id
                          1 :name
                          2 :city
                          3 :country
                          4 :IATA
                          5 :ICAO
                          6 :lat
                          7 :long
                          8 :altitude
                          9 :zone-offset
                          10 :DST
                          11 :zone-id
                          12 :type
                          13 :source})
      (vary-meta assoc :print-column-max-width 15)))


(defonce ds* (delay (obtain-dataset)))


(comment
  (def by-country
    (->> (ds/group-by-column :country @ds*)
         (map (fn [[country country-ds]]
                {:country country
                 :num-airports (ds/row-count country-ds)
                 :average-alt (dfn/mean (country-ds :altitude))}))
         (ds/->>dataset)))

  (ds/sort-by-column :average-alt > by-country)

  (ds/sort-by-column :num-airports > by-country)

  )
