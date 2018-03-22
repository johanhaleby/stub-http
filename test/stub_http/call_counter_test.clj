(ns stub-http.call-counter-test
  (:require [clojure.test :refer :all]
            [stub-http.core :refer :all]
            [cheshire.core :as json]
            [clj-http.lite.client :as client]))


(deftest CounterTest
  (testing "records call count"
    (let [counter (atom 0)]
      (with-routes!
        [body (json/generate-string {:hello "world"})]
        {"/something" {:status 200 :content-type "application/json"
                       :body   body :counter counter}}
        (client/get (str uri "/something")))
        (is (= @counter 1 )))))
