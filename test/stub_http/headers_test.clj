(ns stub-http.headers-test
  (:require [clojure.test :refer :all]
            [stub-http.core :refer :all]
            [clj-http.lite.client :as client]))

(deftest MethodMatching
  (testing "routes can match on method"
    (with-routes!
      {"/something" {:status 200 :content-type "text/plain" :body "hello" :headers {"john" "doe" "ikk" "ikk2"}}}
      (let [headers (:headers (client/get (str uri "/something")))]
        (is (= "doe" (get headers "john")))
        (is (= "ikk2" (get headers "ikk")))))))