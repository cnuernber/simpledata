(ns simpledata.airports
  (:require [tech.v3.dataset :as ds]
            [tech.v3.datatype.functional :as dfn]
            [tech.viz.vega :as vega]))


(defn obtain-dataset
  []
  (-> (ds/->dataset "https://raw.githubusercontent.com/jpatokal/openflights/master/data/airports.dat"
                    {:header-row? false
                     :file-type :csv})
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


(def ds* (delay (obtain-dataset)))


(comment
  (def by-country
    (->> (ds/group-by-column @ds* :country)
         (map (fn [[country country-ds]]
                {:country country
                 :num-airports (ds/row-count country-ds)
                 :average-alt (dfn/mean (country-ds :altitude))}))
         (ds/->>dataset)))

  (ds/sort-by-column by-country :average-alt >)

  (ds/sort-by-column by-country :num-airports >)

  (-> (vega/histogram (@ds* :altitude) "Altitude" {:bin-count 20})
      (vega/vega->svg-file "alt.svg"))

  )
