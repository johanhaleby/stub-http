(ns stub-http.header-match-test
  (:require [clojure.test :refer :all]
            [stub-http.core :refer :all]
            [cheshire.core :as json]
            [clj-http.lite.client :as client])
  (:import (java.net SocketException)))

(defn- post-reply [url & [headers]]
  (->
    url
    (client/post {:body         (json/generate-string {:some-property "some-value"})
                  :content-type :json
                  :headers headers
                  })
     :body
     (json/parse-string true)))

(defn- build-response [value]
  {:status 201 :content-type "application/json" :body (json/generate-string {:name value })})

(deftest HeaderMatching
  (testing "Matching not affected with empty headers"
    (with-routes!
      {{:path    "/none"
        :body    (json/generate-string {:some-property "some-value"})}
       (build-response "none")
       {:path    "/empty"
        :headers {}
        :body    (json/generate-string {:some-property "some-value"})}
        (build-response "empty")
       }
      (let [response-none  (post-reply (str uri "/none"))
            response-empty (post-reply (str uri "/empty"))]
        (is (= "none" (:name response-none)))
        (is (= "empty" (:name response-empty))))))

  (testing "Matching matches header name and value, specified by keyword or string"
    (with-routes!
       {{:path    "/headers"
         :headers { "Authorization" "Important" }
         :body    (json/generate-string {:some-property "some-value"})}
        (build-response "auth")
        {:path    "/headers"
         :headers { :authorization "Keyword-mapped" }
         :body    (json/generate-string {:some-property "some-value"})}
        (build-response "auth")
       }
      (let [response-auth1    (post-reply (str uri "/headers") { "Authorization" "Important"})
            response-auth2    (post-reply (str uri "/headers") { "Authorization" "Keyword-mapped"})]
        (is (= "auth" (:name response-auth1)))
        (is (= "auth" (:name response-auth2)))
        )))

  (testing "Matching won't match if headers not correct"
    (with-routes! {
                   {:path    "/headers"
                    :headers { "Authorization" "Important" }
                    :body    (json/generate-string {:some-property "some-value"})}
                   (build-response "auth")
                  }
                  (is (thrown? SocketException (post-reply (str uri "/headers") {}))))))

