(ns fake-http.manual-close-test
  (:require [clojure.test :refer :all]
            [fake-http.core :refer :all]
            [cheshire.core :as json]
            [clj-http.lite.client :as client]))

(declare ^:dynamic *fake-server*)

(defn start-and-stop-fake-server [f]
  (binding [*fake-server* (start! {{:path "/something"}
                                   {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world"})}})]
    (try
      (f)
      (finally
        (.close *fake-server*)))))

(use-fixtures :each start-and-stop-fake-server)

(deftest MatchesStringPath
  (testing "matches string path"
    (let [response (client/get (str (:uri *fake-server*) "/something"))
          json-response (json/parse-string (:body response) true)]
      (is (= "world" (:hello json-response))))))