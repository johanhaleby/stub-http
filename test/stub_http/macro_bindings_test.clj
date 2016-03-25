(ns stub-http.macro-bindings-test
  (:require [clojure.test :refer :all]
            [stub-http.core :refer :all]
            [cheshire.core :as json]
            [clj-http.lite.client :as client]))

(deftest FakeWebServerMacroWithBindings
  (testing "matches string path"
    (with-routes!
      [body (json/generate-string {:hello "world"})]
      {"/something" {:status 200 :content-type "application/json"
                     :body   body}}
      (let [response (client/get (str uri "/something"))
            json-response (json/parse-string (:body response) true)]
        (is (= "world" (:hello json-response)))))))