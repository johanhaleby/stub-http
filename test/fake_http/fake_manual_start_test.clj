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

(deftest MatchesStringPath
  (testing "matches string path"
    (add-route! *fake-server* "/something" {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world"})})
    (let [response (client/get (str (:uri *fake-server*) "/something"))
          json-response (json/parse-string (:body response) true)]
      (is (= "world" (:hello json-response))))))

(deftest MatchesSpecificQueryParam
  (testing "matches path with specific single query param"
    (add-route! *fake-server* {:path "/something" :query-params {:first "value1"}} {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world2"})})
    (let [response (client/get (str (:uri *fake-server*) "/something?first=value1"))
          json-response (json/parse-string (:body response) true)]
      (is (= "world2" (:hello json-response))))))