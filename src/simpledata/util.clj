(ns simpledata.util
  (:require [tech.io :as io]
            [clojure.tools.logging :as log])
  (:import [java.io InputStream ByteArrayOutputStream]))


(defn url-user-agent->byte-array
  "Yet another downloader. Useful over `slurp` in cases where servers would like
  to see a legit-seeming user-agent string.  It is the caller's responsibility
  to close the returned input stream."
  (^InputStream [url user-agent-string]
   (try
     (-> (java.net.URL. url)
         (.openConnection)
         (doto (.setRequestProperty "User-Agent"
                                    user-agent-string))
         (.getContent))
     (catch Throwable t
       (throw (ex-info "Failed to get-url"
                       {:url url
                        :error t})))))
  (^InputStream [url]
   (url-user-agent->byte-array
    url "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36")))


(defn cache-remote->local-file
  "Download the file if it doesn't exist locally in the cache."
  [remote-fn-or-url dst-url]
  (when-not (io/exists? dst-url)
    (log/infof "Downloading dataset %s" dst-url)
    ;;colornames requires a valid useragent string
    (if (fn? remote-fn-or-url)
      (with-open [ins (remote-fn-or-url)]
        (io/copy ins dst-url))
      (io/copy remote-fn-or-url dst-url)))
  dst-url)
