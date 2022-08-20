(ns stub-http.internal.server
  (:require [clojure.string :refer [split blank? lower-case]]
            [stub-http.internal.functions :refer [map-kv not-nil?]])
  (:import (fi.iki.elonen NanoHTTPD NanoHTTPD$Response$IStatus NanoHTTPD$Response$Status NanoHTTPD$IHTTPSession)
           (java.util HashMap)
           (java.io InputStream)))

(defn- params->map [param-string]
  (when-not (blank? param-string)
    (->> (split param-string #"&")
         (map #(split % #"=" 2))
         (map (fn [[k v]]
                [(keyword k) (when-not (blank? v) v)]))
         (into {}))))

(defn- to-str [o]
  (str (if (keyword? o)
         (name o)
         o)))

(defn- indices-of-routes-matching-request [stub-http-request routes]
  (keep-indexed #(when ((:request-spec-fn %2) stub-http-request) %1) routes))

(defn- create-response
  "Create a nano-httpd Response from the given map.

   path - The request path to mock, for example /search
   status - The status code
   headers - The response headers (list or vector of tuples specifying headers). For example ([\"Content-Type\" \"application/json\"], ...)
   body - The response body
   delay - Delay in ms added to the response"
  [{:keys [status headers body content-type delay counter]}]
  (let [; We see if a predefined status exists for the supplied status code
        ; If no match then we create a custom implementation of IStatus with the supplied status
        status (or (NanoHTTPD$Response$Status/lookup status)
                   (reify NanoHTTPD$Response$IStatus
                     (getDescription [_] "")
                     (getRequestStatus [_]
                       status)))
        nano-response (if (or (nil? body) (string? body))
                        (NanoHTTPD/newFixedLengthResponse status content-type body)
                        (NanoHTTPD/newFixedLengthResponse status content-type body (.available ^InputStream body)))]
    (when delay (Thread/sleep delay))
    (when counter (swap! counter inc))
    (doseq [[name value] headers]
      (.addHeader nano-response (to-str name) (to-str value)))
    nano-response))

(defn- session->stub-request
  "Converts an instance of IHTTPSession to a \"stub-http\" representation of a request"
  [^NanoHTTPD$IHTTPSession session]
  (let [method (->> session .getMethod .toString)
        body-map (HashMap.)
        _ (.parseBody session body-map)
        ; Convert java hashmap into a clojure map
        body-map (into {} body-map)
        ; NanoHTTPD puts the PUT body into a tmp file that gets deleted after the request is handled
        body-map (if (= "PUT" method)
                   (update body-map "content" slurp)
                   body-map)
        path (.getUri session)
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

(defn- find-default-route [routes]
  (some #(when (= (:request-spec %) :default) %) routes))

(defn- contains-default-route? [routes]
  (not-nil? (find-default-route routes)))

(defn new-server!
  "Create a nano server instance that will return the same response over and over on match"
  [port routes]
  (proxy [NanoHTTPD] [port]
    (serve [^NanoHTTPD$IHTTPSession session]
      (let [current-routes @routes
            stub-request (session->stub-request session)
            indicies-matching-request (indices-of-routes-matching-request stub-request current-routes)
            matching-route-count (count indicies-matching-request)]
        ; Check if there's a default route if there's no other match
        (if (and (not= 1 matching-route-count)
                 (contains-default-route? current-routes))
          ; There was no match and a default route so create the response from it
          (create-response ((->> current-routes find-default-route :response-spec-fn) stub-request))
          (do (cond
                (> matching-route-count 1) (throw (ex-info
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
                nano-response)))))))
