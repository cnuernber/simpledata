(ns simpledata.colors
  (:require [tech.v3.dataset :as ds]
            [tech.v3.datatype :as dtype]
            [tech.v3.io :as io]
            [simpledata.sql :as sql]
            [simpledata.util :as util]
            [clojure.tools.logging :as log])
  (:import [java.util.zip ZipFile]
           [smile.neighbor KDTree]))

(defn obtain-dataset
  []
  ;;Useful example of how to set your user agent
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
                        {:parser-fn {:color :string}
                         :key-fn keyword
                         :file-type :csv}))
        (ds/set-dataset-name :colors))))


(defonce dataset* (delay (obtain-dataset)))


(comment

  (ds/descriptive-stats @dataset*)

  (rand-nth (ds/mapseq-reader @dataset*))

  ;;takes time
  (sql/insert-dataset! @dataset*)

  (ds/sort-by-column @dataset* :count >)

  )


(defn kd-tree-inputs
  []
  (-> (assoc @dataset*
             ;;Perform an elemwise mapping specifying the result type.
             :rgb (-> (dtype/emap
                       ;;Type-hinting the color makes this method *much* faster because
                       ;;we are calling the string member function substring over and over
                       ;;again.
                       (fn [name ^String color]
                         (try
                           (double-array
                            [(Integer/parseInt (.substring color 0 2) 16)
                             (Integer/parseInt (.substring color 2 4) 16)
                             (Integer/parseInt (.substring color 4 6) 16)])
                           (catch Throwable e
                             (log/warnf e "Failed to parse color %s-%s" name color)
                             nil)))
                       :object
                       (@dataset* :bestName)
                       (@dataset* :hexCode))
                      ;;Clone to make the mapping concrete as opposed to lazy.
                      ;;Since we are going to filter below and then iterate through it again
                      ;;it avoids reparsing the strings multiple times.  Often clone isn't
                      ;;necessary
                      (dtype/clone)))
      ;;Filter out the mappings that failed.
      (ds/filter-column :rgb identity)))


(defn obtain-kd-tree
  []
  ;;You can destructure datasets like maps.
  (let [{:keys [rgb bestName]} (kd-tree-inputs)]
    ;;rgb is all double arrays so into-array produces an array-of-double-arrays
    (KDTree. (into-array rgb)
             ;;bestName is all strings so it produces an array of strings
             (into-array bestName))))


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
