(ns stub-http.method-test
  (:require [clojure.test :refer :all]
            [stub-http.core :refer :all]
            [cheshire.core :as json]
            [clj-http.lite.client :as client]))

(deftest MethodMatching
  (testing "routes can match on method"
    (with-routes!
      {{:method :GET :path "/something"}  {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world1"})}
       {:method "POST" :path "/something"} {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world2"})}}
      (let [response1 (json/parse-string (:body (client/get (str uri "/something"))) true)
            response2 (json/parse-string (:body (client/post (str uri "/something"))) true)]
        (is (= "world1" (:hello response1)))
        (is (= "world2" (:hello response2)))))))