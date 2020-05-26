(ns simpledata.colors
  (:require [tech.ml.dataset :as ds]
            [tech.io :as io]
            [simpledata.sql :as sql]
            [clojure.tools.logging :as log])
  (:import [java.util.zip ZipFile]
           [smile.neighbor KDTree]))


(defn- get-url
  "Yet another downloader. Useful over `slurp` in cases where servers would like
  to see a legit-seeming user-agent string."
  [url]
  (try
    (let [ua "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36"]
      (with-open [^java.io.InputStream
                  inputstream (-> (java.net.URL. url)
                                  (.openConnection)
                                  (doto (.setRequestProperty "User-Agent" ua))
                                  (.getContent))]
        (let [outputstream (java.io.ByteArrayOutputStream.)]
          (io/copy inputstream outputstream)
          (.toByteArray outputstream))))
    (catch Throwable t
      (throw (ex-info "Failed to get-url"
                      {:url url
                       :t t})))))

(defn obtain-dataset
  []
  (when-not (io/exists? "file://data/colornames.zip")
    (log/info "Downloading Dataset")
    (io/copy (get-url "https://colornames.org/download/colornames.zip")
             "file://data/colornames.zip"))
  (log/info "Decompressing Dataset")
  (let [zipfile (ZipFile. (io/file "file://data/colornames.zip"))
        entry (->> zipfile
                   (.entries)
                   (iterator-seq)
                   (first))]
    (-> (ds/->dataset (.getInputStream zipfile entry)
                      {:header-row? false
                       :parser-fn {0 :string}})
        (ds/set-dataset-name :colors)
        (ds/rename-columns {0 :color
                            1 :name
                            2 :weight
                            3 :count}))))


(defonce dataset* (delay (obtain-dataset)))


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
