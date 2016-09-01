(ns stub-http.modify-route-test
  (:require [clojure.test :refer :all]
            [stub-http.core :refer :all]
            [clj-http.lite.client :as client]
            [cheshire.core :as json]))

(declare ^:dynamic *stub-server*)

(defn start-and-stop-stub-server [f]
  (binding [*stub-server* (start!)]
    (try
      (f)
      (finally
        (.close *stub-server*)))))

(use-fixtures :each start-and-stop-stub-server)

(deftest ModifyingRoute
  (testing "add-route! by request and response specifications"
    (add-route! *stub-server* {:path "/something"} {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world"})})

    (let [response (client/get (str (:uri *stub-server*) "/something"))
          json-response (json/parse-string (:body response) true)]
      (is (= "world" (:hello json-response)))))

  (testing "routes! takes a route-map"
    ; Test thisr
    (routes! *stub-server* {{:path "/something1"} {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world1"})}
                            "/something2"         {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world2"})}})

    (let [base (:uri *stub-server*)
          json-response1 (-> (str base "/something1") client/get :body (json/parse-string true))
          json-response2 (-> (str base "/something2") client/get :body (json/parse-string true))]
      (is (= "world1" (:hello json-response1)))
      (is (= "world2" (:hello json-response2))))))