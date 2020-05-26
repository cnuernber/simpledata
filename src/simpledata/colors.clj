(ns simpledata.colors
  (:require [tech.ml.dataset :as ds]
            [tech.io :as io]
            [simpledata.sql :as sql])
  (:import [java.util.zip ZipFile]))


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
    (io/copy (get-url "https://colornames.org/download/colornames.zip")
             "file://data/colornames.zip"))
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
