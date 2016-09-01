(ns stub-http.core
  (:import (java.io Closeable)
           (fi.iki.elonen NanoHTTPD))
  (:require [clojure.test :refer [function?]]
            [stub-http.internal.port :refer [get-free-port!]]
            [stub-http.internal.functions :refer [not-nil?]]
            [stub-http.internal.server :as nano]
            [stub-http.internal.spec :refer [normalize-request-spec normalize-response-spec]]))

(def ^:private initial-route-state [])

(defn- add-route
  [route-state request-spec response-spec]
  {:pre [(not-nil? route-state) (not-nil? request-spec) (not-nil? response-spec)]}
  (swap! route-state conj {:request-spec-fn  (normalize-request-spec request-spec) :request-spec request-spec
                           :response-spec-fn (normalize-response-spec response-spec) :response-spec response-spec
                           :recordings       []}))

(defn- apply-routes
  "Applies routes by overwriting the existing ones"
  [route-state server routes]
  {:pre [(or (map? routes) (function? routes))]}
  (let [routes-map (if (map? routes)
                     routes
                     (routes server))]
    (assert (map? routes-map))
    (reset! route-state initial-route-state)
    (doseq [[req-spec resp-spec] routes-map]
      (add-route route-state req-spec resp-spec))))

(defprotocol RouteModifier
  (add-route! [this request-spec response-spec] "Add a new route by supplied a request and response specification")
  (routes! [this routes] "Override existing routes with the supplied route map or route function"))

(defprotocol Response
  (recorded-requests [this] "Return all recorded requests")
  (recorded-responses [this] "Return all recorded responses"))

(defrecord NanoFakeServer [nano-server routes]
  Closeable
  (close [_]
    (.stop nano-server))
  RouteModifier
  (add-route! [_ req-spec resp-spec]
    (add-route routes req-spec resp-spec))
  (routes! [_ r]
    (apply-routes routes nano-server r))
  Response
  (recorded-requests [_]
    (mapcat #(map :request (:recordings %)) @routes))
  (recorded-responses [_]
    (mapcat #(map :response (:recordings %)) @routes)))

(defn start!
  "Start a new fake web server on a random free port. Usage example:

  (with-open [server (start!
         {\"something\" {:status 200 :content-type \"application/json\" :body (json/generate-string {:hello \"world\"})}
         {:path \"/y\" :query-params {:q \"something\")}} {:status 200 :content-type \"application/json\" :body  (json/generate-string {:hello \"brave new world\"})}})]
         ; Do actual HTTP request
         )"
  ([] (start! {} {}))
  ([routes] (start! {} routes))
  ([settings routes]
   {:pre [(map? settings) (or (map? routes) (function? routes))]}
   (let [{:keys [port] :or {port (get-free-port!)}} settings
         route-state (atom initial-route-state)
         ^NanoHTTPD nano-server (nano/new-server! port route-state)
         _ (.start nano-server)
         uri (str "http://localhost:" (.getListeningPort nano-server))
         server (map->NanoFakeServer {:uri uri :port port :nano-server nano-server :routes route-state})]
     (apply-routes route-state server routes)
     server)))

(defmacro with-routes!
  "Applies routes and creates and stops a fake server implicitly"
  {:arglists '([bindings? routes & body])}
  [bindings-or-routes & more-args]
  (let [[bindings routes body] (if (vector? bindings-or-routes)
                                 [bindings-or-routes (first more-args) (rest more-args)]
                                 [[] bindings-or-routes more-args])]

    (assert (and (vector? bindings)
                 (even? (count bindings))
                 (every? symbol? (take-nth 2 bindings))) "Bindings need to be a vector with an even number of forms")

    `(let ~bindings
       (let [server# (start! ~routes)
             ~'uri (:uri server#)
             ~'port (:port server#)
             ~'server server#]
         (try
           ~@body
           (finally
             (.close server#)))))))