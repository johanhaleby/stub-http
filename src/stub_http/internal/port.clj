(ns stub-http.internal.port
  (:import (java.net ServerSocket)))

(defn get-free-port! []
  (let [socket (ServerSocket. 0)
        port (.getLocalPort socket)]
    (.close socket)
    port))
