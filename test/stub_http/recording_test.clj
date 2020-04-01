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
    (client/post (str uri "/something")
                 {:body "{\"this-is\": \"json\"}"
                  :content-type :json})
    (client/put (str uri "/something")
                {:body "some PUT data"
                 :accept :json})
    (let [requests (recorded-requests server)
          [req1 req2 req3] requests]
      (is (= 3 (count requests)))

      (is (= "GET /something HTTP/1.1" (:request-line req1)))
      (is (starts-with? (->> req1 :headers :accept) "text/html"))
      (is (false? (contains? req1 :query-params)))
      (is (false? (contains? req1 :body)))

      (is (= "POST /something HTTP/1.1" (:request-line req2)))
      (is (starts-with? (->> req2 :headers :accept) "text/html"))
      (is (= "{\"this-is\": \"json\"}" (get-in req2 [:body "postData"])))
      (is (starts-with? (get-in req2 [:headers :content-type]) "application/json"))

      (is (= "PUT /something HTTP/1.1" (:request-line req3)))
      (is (starts-with? (->> req3 :headers :accept) "application/json"))
      (is (= "some PUT data" (get-in req3 [:body "content"]))))))

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
