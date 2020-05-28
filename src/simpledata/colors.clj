(ns simpledata.colors
  (:require [tech.ml.dataset :as ds]
            [tech.io :as io]
            [simpledata.sql :as sql]
            [simpledata.util :as util]
            [clojure.tools.logging :as log])
  (:import [java.util.zip ZipFile]
           [smile.neighbor KDTree]))

(defn obtain-dataset
  []
  (with-open [zipfile (-> (util/cache-remote->local-file
                     #(util/url-user-agent->byte-array
                       "https://colornames.org/download/colornames.zip"
                       "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36")
                     "file://download/colornames.zip")
                    (io/file)
                    (ZipFile.))]
    (log/infof "Decompressing dataset colornames")
    (-> zipfile
        (.entries)
        (iterator-seq)
        (first)
        (#(ds/->dataset (.getInputStream zipfile %)
                        {:header-row? false
                         :parser-fn {0 :string}}))
        (ds/set-dataset-name :colors)
        (ds/rename-columns {0 :color
                            1 :name
                            2 :weight
                            3 :count}))))


(def dataset* (delay (obtain-dataset)))


(comment

  (ds/descriptive-stats @dataset*)

  (ds/rand-nth (ds/mapseq-reader @dataset*))

  ;;takes time
  (sql/insert-dataset! @dataset*)

  (ds/sort-by-column 3 > @dataset*)

  )


(defn kd-tree-inputs
  []
  {:rgb (->> (@dataset* :color)
             (map (fn [color]
                    (double-array
                     [(Integer/parseInt (.substring color 0 2) 16)
                      (Integer/parseInt (.substring color 2 4) 16)
                      (Integer/parseInt (.substring color 4 6) 16)])) ))
   :names (@dataset* :name)})


(defn obtain-kd-tree
  []
  (let [{:keys [rgb names]} (kd-tree-inputs)]
    (KDTree. (into-array rgb)
             (into-array names))))

(defonce kd-tree* (delay (obtain-kd-tree)))

(defn knn
  [rgb n]
  (->> (.knn @kd-tree* (double-array rgb) n)
       (map (fn [neighbor]
              {:distance (.distance neighbor)
               :rgb (vec (.key neighbor))
               :name (.value neighbor)}))
       (sort-by :distance)))


(comment
  (knn [0xff 0x88 0x88] 10)
  )
