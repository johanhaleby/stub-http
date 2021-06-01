(ns stub-http.body-test
  (:require [clojure.test :refer :all]
            [stub-http.core :refer :all]
            [cheshire.core :as json]
            [clj-http.lite.client :as client]))

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
