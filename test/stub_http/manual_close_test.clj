(ns stub-http.manual-close-test
  (:require [clojure.test :refer :all]
            [stub-http.core :refer :all]
            [cheshire.core :as json]
            [clj-http.lite.client :as client]))

(declare ^:dynamic *stub-server*)

(defn start-and-stop-stub-server [f]
  (binding [*stub-server* (start! {{:path "/something"}
                                   {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world"})}})]
    (try
      (f)
      (finally
        (.close *stub-server*)))))

(use-fixtures :each start-and-stop-stub-server)

(deftest MatchesStringPath
  (testing "matches string path"
    (let [response (client/get (str (:uri *stub-server*) "/something"))
          json-response (json/parse-string (:body response) true)]
      (is (= "world" (:hello json-response))))))