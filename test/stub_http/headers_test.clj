(ns stub-http.headers-test
  (:require [clojure.test :refer :all]
            [stub-http.core :refer :all]
            [clj-http.lite.client :as client]))

(deftest HeaderMatching
  (testing "headers can be defined as string - string pairs"
    (with-routes!
      {"/something" {:status 200 :content-type "text/plain" :body "hello" :headers {"john" "doe" "ikk" "ikk2"}}}
      (let [headers (:headers (client/get (str uri "/something")))]
        (is (= "doe" (get headers "john")))
        (is (= "ikk2" (get headers "ikk"))))))

  (testing "headers can be defined as keyword - string pairs"
    (with-routes!
      {"/something" {:status 200 :content-type "text/plain" :body "hello" :headers {:john "doe" :ikk "ikk2"}}}
      (let [headers (:headers (client/get (str uri "/something")))]
        (is (= "doe" (get headers "john")))
        (is (= "ikk2" (get headers "ikk"))))))

  (testing "headers can be defined as keyword - int pairs"
    (with-routes!
      {"/something" {:status 200 :content-type "text/plain" :body "hello" :headers {"john" 2 :ikk 3}}}
      (let [headers (:headers (client/get (str uri "/something")))]
        (is (= "2" (get headers "john")))
        (is (= "3" (get headers "ikk")))))))