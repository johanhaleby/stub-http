(ns fake-http.fake-auto-start-test
  (:require [clojure.test :refer :all]
            [fake-http.fake :refer :all]
            [cheshire.core :as json]
            [clj-http.lite.client :as client]))

(deftest FakeWebServerMacro
  (testing "matches string path"
    (with-routes!
      {"/something" {:status 200 :content-type "application/json"
                     :body   (json/generate-string {:hello "world"})}}
      (let [response (client/get (str uri "/something"))
            json-response (json/parse-string (:body response) true)]
        (is (= "world" (:hello json-response)))))))