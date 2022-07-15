(ns stub-http.body-test
  (:require [clojure.test :refer :all]
            [stub-http.core :refer :all]
            [cheshire.core :as json]
            [clj-http.lite.client :as client])
  (:import (java.io ByteArrayInputStream)))

(deftest BodyMatching
  (testing "routes can match on body for post request"
    (with-routes!
      {{:path   "/something"
        :body   (json/generate-string {:some-property "some-value"})}
       {:status 201 :content-type "application/json" :body (json/generate-string {:hello "world1"})}
       {:path   "/something"
        :body   (json/generate-string {:some-property "different-value"})}
       {:status 201 :content-type "application/json" :body (json/generate-string {:hello "world2"})}}
      (let [response1 (->
                        (str uri "/something")
                        (client/post {:body         (json/generate-string {:some-property "some-value"})
                                      :content-type :json})
                        :body
                        (json/parse-string true))
            response2 (->
                        (str uri "/something")
                        (client/post {:body         (json/generate-string {:some-property "different-value"})
                                      :content-type :json})
                        :body
                        (json/parse-string true))]
        (is (= "world1" (:hello response1)))
        (is (= "world2" (:hello response2))))))

  (testing "routes can handle binary payloads"
    (with-routes!
      {{:path         "/something"
        :content-type "application/x-binary"
        :accept       "application/x-binary"
        :body         "A binary request"}
       {:status 201 :content-type "application/x-binary" :body (-> "A binary response" (String.) (.getBytes) (ByteArrayInputStream.))}}
      (let [response (->
                        (str uri "/something")
                        (client/post {:body         (-> "A binary request" (String.) (.getBytes) (ByteArrayInputStream.))
                                      :content-type :application/x-binary
                                      :accept       :application/x-binary})
                        :body
                        (str))]
        (is (= "A binary response" response)))))

  (testing "routes can match on body for put request"
    (with-routes!
      {{:path   "/something"
        :body   (json/generate-string {:some-property "some-value"})}
       {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world1"})}
       {:path   "/something"
        :body   (json/generate-string {:some-property "different-value"})}
       {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world2"})}}
      (let [response1 (->
                        (str uri "/something")
                        (client/put {:body          (json/generate-string {:some-property "some-value"})
                                      :content-type :json})
                        :body
                        (json/parse-string true))
            response2 (->
                        (str uri "/something")
                        (client/put {:body          (json/generate-string {:some-property "different-value"})
                                      :content-type :json})
                        :body
                        (json/parse-string true))]
        (is (= "world1" (:hello response1)))
        (is (= "world2" (:hello response2)))))))
