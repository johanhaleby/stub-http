(ns fake-http.fake-manual-start-test
  (:require [clojure.test :refer :all]
            [fake-http.fake :refer :all]
            [cheshire.core :as json]
            [clj-http.lite.client :as client]))

(declare ^:dynamic *fake-server*)

(defn start-and-stop-fake-server [f]
  (binding [*fake-server* (start!)]
    (try
      (f)
      (finally
        (shutdown! *fake-server*)))))

(use-fixtures :each start-and-stop-fake-server)

(deftest FakeWebServer
  (testing "matches string path"
    (fake-route! *fake-server* "/something" {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world"})})
    (let [response (client/get (str (uri *fake-server*) "/something"))
          json-response (json/parse-string (:body response) true)]
      (is (= "world" (:hello json-response)))))

  (testing "matches path with specific single query param"
    (fake-route! *fake-server* {:path "/something" :query {:first "value1"}} {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world2"})})
    (let [response (client/get (str (uri *fake-server*) "/something?first=value1"))
          json-response (json/parse-string (:body response) true)]
      (is (= "world2" (:hello json-response)))))

  (testing "matches path with most specific single query param"
    (fake-route! *fake-server* {:path "/something"} {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world1"})})
    (fake-route! *fake-server* {:path "/something" :query {:first "value1"}} {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world2"})})
    (let [response (client/get (str (uri *fake-server*) "/something?first=value1"))
          json-response (json/parse-string (:body response) true)]
      (is (= "world2" (:hello json-response))))))