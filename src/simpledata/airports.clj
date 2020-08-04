(ns simpledata.airports
  (:require [tech.ml.dataset :as ds]
            [tech.v2.datatype.functional :as dfn]
            [tech.viz.vega :as vega]))


(defn obtain-dataset
  []
  (-> (ds/->dataset "https://raw.githubusercontent.com/jpatokal/openflights/master/data/airports.dat"
                    {:header-row? false})
      (ds/rename-columns {"column-0" :id
                          "column-1" :name
                          "column-2" :city
                          "column-3" :country
                          "column-4" :IATA
                          "column-5" :ICAO
                          "column-6" :lat
                          "column-7" :long
                          "column-8" :altitude
                          "column-9" :zone-offset
                          "column-10" :DST
                          "column-11" :zone-id
                          "column-12" :type
                          "column-13" :source})
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

  (-> (vega/histogram (@ds* :altitude) "Altitude" {:bin-count 20})
      (vega/vega->svg-file "alt.svg"))

  )
