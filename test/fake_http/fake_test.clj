(ns fake-http.fake-test
  (:require [fake-http.fake :refer :all]
            [speclj.core :refer :all]
            [cheshire.core :as json]
            [clj-http.lite.client :as client]))

(def server (atom nil))

(describe
  "Fake Web Server"

  (around [it]
          (reset! server (start!))
          (try
            (it)
            (finally
              (shutdown! @server)
              (reset! server nil))))

  (it "matches string path"
      (fake-route! @server "/something" {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world"})})
      (let [response (client/get (str (uri @server) "/something"))
            json-response (json/parse-string (:body response) true)]
        (should= "world" (:hello json-response))))

  (it "matches path with specific single query param"
      (fake-route! @server {:path "/something" :query {:first "value1"}} {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world2"})})
      (let [response (client/get (str (uri @server) "/something?first=value1"))
            json-response (json/parse-string (:body response) true)]
        (should= "world2" (:hello json-response))))

  (it "matches path with most specific single query param"
      (fake-route! @server {:path "/something" } {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world1"})})
      (fake-route! @server {:path "/something" :query {:first "value1"}} {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world2"})})
      (let [response (client/get (str (uri @server) "/something?first=value1"))
            json-response (json/parse-string (:body response) true)]
        (should= "world2" (:hello json-response)))))
