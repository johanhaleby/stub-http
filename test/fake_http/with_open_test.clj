(ns fake-http.with-open-test
  (:require [clojure.test :refer :all]
            [fake-http.core :refer :all]
            [cheshire.core :as json]
            [clj-http.lite.client :as client]))

(deftest FakeWebServerUsingWithOpen
  (testing "matches string path"
    (with-open [server (start! {"/something" {:status 200 :content-type "application/json"
                                              :body   (json/generate-string {:hello "world"})}})]
      (let [response (client/get (str (:uri server) "/something"))
            json-response (json/parse-string (:body response) true)]
        (is (= "world" (:hello json-response))))))

  (testing "can use predefined port"
    (with-open [_ (start! {:port 8087}
                          {"/something" {:status 200 :content-type "application/json"
                                         :body   (json/generate-string {:hello "world"})}})]
      (let [response (client/get "http://localhost:8087/something")
            json-response (json/parse-string (:body response) true)]
        (is (= "world" (:hello json-response))))))

  (testing "matches path with specific single query param"
    (with-open [server (start! {{:path "/something" :query-params {:first "value1"}}
                                {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world2"})}})]
      (let [response (client/get (str (:uri server) "/something?first=value1"))
            json-response (json/parse-string (:body response) true)]
        (is (= "world2" (:hello json-response))))))

  (testing "can start multiple servers in the same with-open statement"
    (with-open [server1 (start! {"/something" {:status 200 :content-type "application/json"
                                               :body   (json/generate-string {:hello "world"})}})
                server2 (start! {"/something" {:status 200 :content-type "application/json"
                                               :body   (json/generate-string {:hello "world2"})}})]
      (let [response1 (client/get (str (:uri server1) "/something"))
            response2 (client/get (str (:uri server2) "/something"))
            json-response1 (json/parse-string (:body response1) true)
            json-response2 (json/parse-string (:body response2) true)]
        (is (= "world" (:hello json-response1)))
        (is (= "world2" (:hello json-response2)))))))