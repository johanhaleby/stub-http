(ns stub-http.internal.spec
  (:require [clojure.string :refer (lower-case)]
            [stub-http.internal.functions :refer [substring-before]])
  (:import (clojure.lang PersistentArrayMap IPersistentMap IFn Keyword)))

(defn- request-spec-matches? [request-spec request]
  (letfn [(path-without-query-params [path]
            (substring-before path "?"))
          (method-matches? [expected-method actual-method]
            (let [; Make keyword out of string and never mind "case" of keyword (i.e. :GET and :get are treated the same)
                  normalize (comp keyword lower-case name)
                  expected-normalized (normalize (or expected-method actual-method)) ; Assume same as actual if not present
                  actual-normalized (normalize actual-method)]
              (= expected-normalized actual-normalized)))
          (path-matches? [expected-path actual-path]
            (= (or expected-path actual-path) actual-path))
          (query-param-matches? [expected-params actual-params]
            (let [expected-params (or expected-params {})   ; Assume empty map if nil
                  query-params-to-match (select-keys actual-params (keys expected-params))]
              (= expected-params query-params-to-match)))
          (body-matches? [expected-body actual-body]
            (let [expected-body (or expected-body actual-body)]   ; Assume match if empty
              (= expected-body actual-body)))]
    (and (apply path-matches? (map (comp path-without-query-params :path) [request-spec request]))
         (query-param-matches? (:query-params request-spec) (:query-params request))
         (method-matches? (:method request-spec) (:method request))
         (body-matches? (:body request-spec) (get-in request [:body "postData"])))))

(defn- throw-normalization-exception! [type ^Object val]
  (let [class-name (-> val .getClass .getName)
        error-message (str "Internal error: Couldn't find " type " conversion strategy for class " (-> val .getClass .getName))]
    (throw (ex-info error-message {:class class-name :value val}))))

(defmulti normalize-request-spec class)
(defmethod normalize-request-spec IFn [req-fn] req-fn)
(defmethod normalize-request-spec IPersistentMap [req-spec] (fn [request] (request-spec-matches? req-spec request)))
(defmethod normalize-request-spec PersistentArrayMap [req-spec] (fn [request] (request-spec-matches? req-spec request)))
(defmethod normalize-request-spec String [path] (normalize-request-spec {:path path}))
(defmethod normalize-request-spec Keyword [key] (if (= :default key) ; Allow for default route
                                                  (constantly false) ; Default match should never match, it's handled explicitly
                                                  (throw (IllegalArgumentException. "Only :default is a valid keyword for the request specification"))))
(defmethod normalize-request-spec :default [value] (throw-normalization-exception! "request" value))

(defmulti normalize-response-spec class)
(defmethod normalize-response-spec IFn [resp-fn] resp-fn)
(defmethod normalize-response-spec IPersistentMap [resp-map] (fn [_] resp-map))
(defmethod normalize-response-spec PersistentArrayMap [resp-map] (fn [_] resp-map))
(defmethod normalize-response-spec String [body] (fn [_] {:status 200 :content-type "text/plain" :headers {:content-type "text/plain"} :body body}))
(defmethod normalize-response-spec :default [value] (throw-normalization-exception! "response" value))

