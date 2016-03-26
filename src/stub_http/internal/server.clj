(ns stub-http.internal.server
  (:require [clojure.string :refer [split blank? lower-case]]
            [stub-http.internal.functions :refer [map-kv keywordize-keys substring-before]])
  (:import (fi.iki.elonen NanoHTTPD NanoHTTPD$Response$IStatus NanoHTTPD$Response$Status NanoHTTPD$IHTTPSession)
           (java.util HashMap)))

(defn- params->map [param-string]
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

(defn- indices-of-route-matching-stub-request [stub-http-request routes]
  (keep-indexed #(if (true? ((:request-spec-fn %2) stub-http-request)) %1 nil) routes))

(defn- create-response [{:keys [status headers body content-type]}]
  "Create a nano-httpd Response from the given map.

   path - The request path to mock, for example /search
   status - The status code
   headers - The response headers (list or vector of tuples specifying headers). For example ([\"Content-Type\" \"application/json\"], ...)
   body - The response body"
  (let [; We see if a predefined status exists for the supplied status code
        istatus (first (filter #(= (.getRequestStatus %) status) (NanoHTTPD$Response$Status/values)))
        ; If no match then we create a custom implementation of IStatus with the supplied status
        istatus (or istatus (reify NanoHTTPD$Response$IStatus
                              (getDescription [_] "")
                              (getRequestStatus [_]
                                status)))

        nano-response (NanoHTTPD/newFixedLengthResponse istatus content-type body)]
    (for [[name value] headers]
      (.addHeader nano-response name value))
    nano-response))

(defn- session->stub-request
  "Converts an instance of IHTTPSession to a \"stub-http\" representation of a request"
  [^NanoHTTPD$IHTTPSession session]
  (let [body-map (HashMap.)
        _ (.parseBody session body-map)
        ; Convert java hashmap into a clojure map
        body-map (into {} body-map)
        path (.getUri session)
        method (->> session .getMethod .toString)
        headers (->> (.getHeaders session)
                     (map-kv (comp keyword lower-case) identity))
        req {:method       method
             :headers      headers
             :content-type (:content-type :headers)
             :path         path
             ; There's no way to get the protocol version now? File an issue!
             :request-line (str method " " path " HTTP/1.1")
             :body         (if (empty? body-map) nil body-map)
             :query-params (params->map (.getQueryParameterString session))}
        req-no-nils (into {} (filter (comp some? val) req))]
    req-no-nils))

(defn new-server! [port routes]
  "Create a nano server instance that will return the same response over and over on match"
  (proxy [NanoHTTPD] [port]
    (serve [^NanoHTTPD$IHTTPSession session]
      (let [current-routes @routes
            stub-request (session->stub-request session)
            indicies-matching-request (indices-of-route-matching-stub-request stub-request current-routes)
            matching-route-count (count indicies-matching-request)]
        (cond
          ; TODO Make this configurable by allowing to determine what should happen by supplying a :default response
          (> matching-route-count 1)
          (throw (ex-info
                   (str "Failed to determine response since several routes matched request: " stub-request ". Matching response specs are:\n")
                   {:matching-specs (map #(select-keys % [:request-spec :response-spec]) ; Select only the request spec and response spec
                                         ; Get the routes for the matching indicies
                                         (mapv current-routes indicies-matching-request))}))
          (= matching-route-count 0) (throw (ex-info (str "Failed to determine response since no route matched request: " stub-request ". Routes are:\n")
                                                     {:routes current-routes})))
        (let [index-matching (first indicies-matching-request)
              response-fn (:response-spec-fn (get current-routes index-matching))
              stub-response (response-fn stub-request)
              nano-response (create-response stub-response)]
          ; Record request
          (swap! routes update-in [index-matching :recordings] (fn [invocations]
                                                                 (conj invocations
                                                                       {:request  stub-request
                                                                        :response stub-response})))
          nano-response)))))
