(ns stub-http.internal.server-test
  (:require [clojure.test :refer (are deftest)]
            [stub-http.internal.server :as server]))

(deftest exercise-params->map
  (are [input expected]
       (= expected (#'server/params->map input))

    nil nil
    "" nil
    "foo=bar" {:foo "bar"}
    "foo=bar&baz=quux" {:foo "bar" :baz "quux"}
    "foo=&baz=quux" {:foo nil :baz "quux"}
    "foo=bar&baz=" {:foo "bar" :baz nil}
    "foo=bar=baz&bar=&baz=quux" {:foo "bar=baz" :bar nil :baz "quux"}))
