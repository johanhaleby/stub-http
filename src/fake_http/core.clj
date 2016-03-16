(ns fake-http.core
  (:import (fi.iki.elonen NanoHTTPD NanoHTTPD$IHTTPSession NanoHTTPD$Response$Status NanoHTTPD$Response$IStatus)
           (clojure.lang IFn IPersistentMap PersistentArrayMap)
           (java.net ServerSocket)
           (java.util HashMap)
           (java.io Closeable))
  (:require [clojure.string :refer [split blank? lower-case]]
            [clojure.test :refer [function?]]))

(defn map-kv [fk fv m]
  "Maps the keys and values in a map with the supplied functions.
  fk = the function for the keys
  fv = the function for the values"
  (into {} (for [[k v] m] [(fk k) (fv v)])))

(defn- keywordize-keys
  "Recursively transforms all map keys from strings to keywords."
  [m]
  (let [f (fn [[k v]] (if (string? k) [(keyword k) v] [k v]))]
    (clojure.walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn- substring-before [str before]
  "Return the substring of string <str> before string <before>. If no match then <str> is returned."
  (let [index-of-before (.indexOf str before)]
    (if (= -1 index-of-before)
      str
      (.substring str 0 index-of-before))))

(defn- params->map [param-string]
  (if-not (blank? param-string)
    (let [params-splitted-by-and (split param-string #"&")
          ; Handle no-value parameters. If a no-value parameter is found then use nil as parameter value
          params-as-tuples (map #(let [index-of-= (.indexOf % "=")]
                                  (if (and
                                        (> index-of-= 0)
                                        ; Check if the first = char is the last char of the param.
                                        (< (inc index-of-=) (.length %)))
                                    (split % #"=")
                                    [% nil])) params-splitted-by-and)
          param-list (flatten params-as-tuples)]
      (keywordize-keys (apply hash-map param-list)))))

(defn- fake-response [{:keys [status headers body content-type]}]
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

(defn- route->request-spec [fake-http-request {:keys [request-spec-fn]}]
  (request-spec-fn fake-http-request))

(defn- http-session->fake-http-request
  "Converts an instance of IHTTPSession to a \"fake-http\" representation of a request"
  [^NanoHTTPD$IHTTPSession session]
  (let [body-map (HashMap.)
        _ (.parseBody session body-map)
        ; Convert java hashmap into a clojure map
        body-map (into {} body-map)]
    {:method       (->> session .getMethod .toString)
     :headers      (->> (.getHeaders session)
                        (map-kv lower-case vec))
     :content-type (last (get :headers "content-type"))
     :request-line (.getParms session)
     :path         (.getUri session)
     :body         body-map
     :query-params (params->map (.getQueryParameterString session))}))

(defn- new-nano-server! [port routes]
  "Create a nano server instance that will return the same response over and over on match"
  (proxy [NanoHTTPD] [port]
    (serve [^NanoHTTPD$IHTTPSession session]
      (let [routes @routes
            fake-http-request (http-session->fake-http-request session)
            routes-matching-request (filter (partial route->request-spec fake-http-request) routes)
            matching-route-count (count routes-matching-request)]
        (cond
          (> matching-route-count 1) (throw (ex-info "Failed to determine response since several routes match" {:routes routes}))
          ; TODO Make this configurable by allowing to determine what should happen by supplying a :default response
          (= matching-route-count 0) (throw (ex-info "Failed to determine response since no route matches" {:routes routes})))
        (let [response-fn (:response-spec-fn (first routes-matching-request))
              response-spec (response-fn fake-http-request)]
          (fake-response response-spec))))))

(defn- record-route! [route-state route-matcher-fn response-fn]
  (swap! route-state conj {:request-spec-fn route-matcher-fn :response-spec-fn response-fn}))

(defn- request-spec-matches? [request-spec request]
  (letfn [(path-without-query-params [path]
            (substring-before path "?"))
          (path-matches? [expected-path actual-path]
            (= (or expected-path actual-path) actual-path))
          (query-param-matches? [expected-params actual-params]
            (let [expected-params (or expected-params {})   ; Assume empty map if nil
                  query-params-to-match (select-keys actual-params (keys expected-params))]
              (= expected-params query-params-to-match)))]
    (and (apply path-matches? (map (comp path-without-query-params :path) [request-spec request]))
         (query-param-matches? (:query-params request-spec) (:query-params request)))))

(defn- throw-normalize-exception! [type val]
  (let [class-name (-> val .getClass .getName)
        error-message (str "Internal error: Couldn't find " type " conversion strategy for class " (-> val .getClass .getName))]
    (throw (ex-info error-message {:class class-name :value val}))))

(defmulti normalize-request-spec class)
(defmethod normalize-request-spec IFn [req-fn] req-fn)
(defmethod normalize-request-spec IPersistentMap [req-spec] (fn [request] (request-spec-matches? req-spec request)))
(defmethod normalize-request-spec PersistentArrayMap [req-spec] (fn [request] (request-spec-matches? req-spec request)))
(defmethod normalize-request-spec String [path] (normalize-request-spec {:path path}))
(defmethod normalize-request-spec :default [value] (throw-normalize-exception! "request" value))

(defmulti normalize-response-spec class)
(defmethod normalize-response-spec IFn [resp-fn] resp-fn)
(defmethod normalize-response-spec IPersistentMap [resp-map] (fn [_] resp-map))
(defmethod normalize-response-spec PersistentArrayMap [resp-map] (fn [_] resp-map))
(defmethod normalize-response-spec String [body] (fn [_] {:status 200 :content-type "text/plain" :headers {:content-type "text/plain"} :body body}))
(defmethod normalize-response-spec :default [value] (throw-normalize-exception! "response" value))

(defn- get-free-port! []
  (let [socket (ServerSocket. 0)
        port (.getLocalPort socket)]
    (.close socket)
    port))

(defrecord NanoFakeServer [nano-server routes]
  Closeable
  (close [_]
    (println "Closing" (.getListeningPort nano-server))
    (.stop nano-server)))

(defn start!
  "Start a new fake web server on a random free port. Usage example:

  (with-open [server (start!
         {\"something\" {:status 200 :content-type \"application/json\" :body (json/generate-string {:hello \"world\"})}
         {:path \"/y\" :query-params {:q \"something\")}} {:status 200 :content-type \"application/json\" :body  (json/generate-string {:hello \"brave new world\"})}})]
         ; Do actual HTTP request
         )"
  ([routes] (start! {} routes))
  ([settings routes]
   {:pre [(map? settings) (map? routes)]}
   (let [{:keys [port] :or {port (get-free-port!)}} settings
         route-state (atom [])
         normalized-routes (map-kv normalize-request-spec normalize-response-spec routes)
         nano-server (new-nano-server! port route-state)
         _ (.start nano-server)
         uri (str "http://localhost:" (.getListeningPort nano-server))]
     (doseq [[req-spec resp-spec] normalized-routes]
       (record-route! route-state req-spec resp-spec))
     (map->NanoFakeServer {:uri uri :port port :nano-server nano-server :routes route-state}))))

(defmacro with-routes!
  "Applies routes and creates and stops a fake server implicitly"
  [routes & body]
  `(let [routes# ~routes]
     (let [server# (start! routes#)
           ~'uri (:uri server#)]
       (try
         ~@body
         (finally
           (.close server#))))))