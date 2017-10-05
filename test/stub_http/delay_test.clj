(ns stub-http.delay-test
  (:require [clojure.test :refer :all]
            [stub-http.core :refer :all]
            [cheshire.core :as json]
            [clj-http.lite.client :as client]))

(defmacro bench
  ([& forms]
   `(let [start# (System/currentTimeMillis)]
      ~@forms
      (- (System/currentTimeMillis) start#))))

(deftest DelayTest
  (testing "add delay to response"
    (with-routes!
      [body (json/generate-string {:hello "world"})]
      {"/something" {:status 200 :content-type "application/json"
                     :body   body :delay 1000}}
      (is (> (bench (client/get (str uri "/something"))) 1000 )))))