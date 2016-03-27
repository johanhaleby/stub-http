(ns stub-http.default-route-test
  (:require [clojure.test :refer :all]
            [stub-http.core :refer :all]
            [clj-http.lite.client :as client]))

(deftest DefaultRoute
  (testing "can specify a default route whose response is returned when no other route matches"
    (with-routes!
      {"/hello" {:status 200 :content-type "text/plain" :body "Hello"}
       :default {:status 200 :content-type "text/plain" :body "World"}}
      (let [response (:body (client/get (str uri "/random")))]
        (is (= "World" response)))))

  (testing "can only specify :default as keyword in request spec"
    (is (thrown-with-msg? IllegalArgumentException #"Only :default is a valid keyword for the request specification"
                          (with-routes! {:test {:status 200 :content-type "text/plain" :body "World"}})))))