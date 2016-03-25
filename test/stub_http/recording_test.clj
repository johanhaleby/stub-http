(ns stub-http.recording-test
  (:require [clojure.test :refer :all]
            [stub-http.core :refer :all]
            [cheshire.core :as json]
            [clojure.string :refer [starts-with?]]
            [clj-http.lite.client :as client]))

(deftest RecordedRequestsTest
  (with-routes!
    {"/something" {:status 200 :content-type "application/json"
                   :body   (json/generate-string {:hello "world"})}}
    (client/get (str uri "/something"))
    (client/get (str uri "/something"))
    (let [requests (recorded-requests server)
          [req1 req2] requests]
      (is (= 2 (count requests)))
      (is (starts-with? (->> req1 :headers :accept) "text/html"))
      (is (starts-with? (->> req2 :headers :accept) "text/html"))
      (is (false? (contains? req1 :query-params)))
      (is (false? (contains? req1 :body)))
      (is (= "GET /something HTTP/1.1" (:request-line req2))))))

(deftest RecordedResponsesTest
  (with-routes!
    {"/something" {:status 200 :content-type "application/json"
                   :body   (json/generate-string {:hello "world"})}}
    (client/get (str uri "/something"))
    (client/get (str uri "/something"))
    (let [responses (recorded-responses server)
          [resp1 resp2] responses]
      (is (= 200 (:status resp1)))
      (is (= 200 (:status resp2)))
      (is (= "application/json" (:content-type resp1)))
      (is (= "application/json" (:content-type resp2)))
      (is (= "{\"hello\":\"world\"}" (:body resp1)))
      (is (= "{\"hello\":\"world\"}" (:body resp2))))))