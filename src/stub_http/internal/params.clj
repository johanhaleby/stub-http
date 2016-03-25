(ns stub-http.internal.params
  (:require [clojure.string :refer [split blank? lower-case]]
            [stub-http.internal.support :refer [map-kv keywordize-keys substring-before]]))

(defn params->map [param-string]
  (if-not (blank? param-string)
    (let [params-splitted-by-ampersand (split param-string #"&")
          ; Handle no-value parameters. If a no-value parameter is found then use nil as parameter value
          param-list (mapcat #(let [index-of-= (.indexOf % "=")]
                               (if (and
                                     (> index-of-= 0)
                                     ; Check if the first = char is the last char of the param.
                                     (< (inc index-of-=) (.length %)))
                                 (split % #"=")
                                 [% nil])) params-splitted-by-ampersand)]
      (keywordize-keys (apply hash-map param-list)))))
