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

  (it "Matches string path"
      (fake-route! @server "/something" {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world"})})
      (let [response (client/get (str (uri @server) "/something"))
            json-response (json/parse-string (:body response) true)]
        (should= (:hello json-response) "world"))))
