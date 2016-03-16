(ns fake-http.routes-as-fn-test
  (:require [clojure.test :refer :all]
            [fake-http.core :refer :all]
            [cheshire.core :as json]
            [clj-http.lite.client :as client]))

(deftest FakeWebServerRoutesAsFunction
  (testing "can use a function as route"
    (with-routes!
      (fn [server]
        {"/something" {:status 200 :content-type "application/json"
                       :body   (json/generate-string {:hello (:uri server)})}})
      (let [response (client/get (str uri "/something"))
            json-response (json/parse-string (:body response) true)]
        (is (= uri (:hello json-response)))))))