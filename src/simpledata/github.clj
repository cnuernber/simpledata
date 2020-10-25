(ns simpledata.github
  (:require [tech.v3.dataset :as ds]
            [tech.v3.io :as io]
            [tech.viz.vega :as vega]))


(defn obtain-dataset
  []
  (-> (io/get-json "https://api.github.com/events"
                   :key-fn keyword)
      (ds/->dataset)
      (vary-meta assoc
                 :print-line-policy :single
                 :print-column-max-width 15)))


(defonce ds* (delay (obtain-dataset)))


(comment

  (->> ((obtain-dataset) :type)
       (frequencies)
       (sort-by second >))

  )
