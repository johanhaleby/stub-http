# ![Logo](logo.png "Stub HTTP") [![Build Status](https://img.shields.io/travis/johanhaleby/stub-http.svg?style=flat)](https://travis-ci.org/johanhaleby/stub-http) [![MIT License](https://img.shields.io/badge/license-MIT-brightgreen.svg?style=flat)](https://www.tldrlegal.com/l/mit) 

A Clojure library designed to stub HTTP responses regardless of which library used to actually make the HTTP requests.
  
There are several client specific "http mocking/stubbing/faking" libraries out there such as [clj-http-fake](https://github.com/myfreeweb/clj-http-fake) and 
[ring-mock](https://github.com/ring-clojure/ring-mock) but they work on the level of the library and not the HTTP level. I couldn't find a client agnostic library for 
stubbing HTTP endpoints so I sat out to write one myself based on [nanohttpd](https://github.com/NanoHttpd/nanohttpd). This is useful
if you want to test your app against a real HTTP server with actual HTTP requests. And even if you don't _want_ to this it may be your only
option if you're (for example) is using a Java library that makes HTTP requests and you want to stub/fake its responses.

More docs and implementation is coming soon.

## Latest version

The latest release version of stub-http is hosted on [Clojars](https://clojars.org):

[![Current Version](https://clojars.org/se.haleby/stub-http/latest-version.svg)](https://clojars.org/se.haleby/stub-http)

## Usage

```clojure
(with-routes! 
	{"something" {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world"})}
	 {:path "/y" :query-params {:q "something")}} {:status 200 :content-type "application/json" :body  (json/generate-string {:hello "brave new world"})}}
	 ; Do actual HTTP request
	 )
```

## Documentation

See [wiki](https://github.com/johanhaleby/stub-http/wiki).

## Full Examples

### Example 1 - Simple Macro
Example demonstrating integration with clojure test and [clj-http-lite](https://github.com/hiredman/clj-http-lite). 
This example matches path "/something" and returns the json document
     
```javascript
{ "hello" : "world" }
```

as response: 

```clojure
(ns stub-http.example1
  (:require [clojure.test :refer :all]
            [stub-http.core :refer :all]
            [cheshire.core :as json]
            [clj-http.lite.client :as client]))

(deftest Example1  
    (with-routes!
      {"/something" {:status 200 :content-type "application/json"
                     :body   (json/generate-string {:hello "world"})}}
      (let [response (client/get (str uri "/something"))
            json-response (json/parse-string (:body response) true)]
        (is (= "world" (:hello json-response))))))
```

### Example 2 - Explicit Start and Stop

This example matches only a GET request with a query param "name" equal "value" using the `start!` function:

```clojure
(ns stub-http.example2
  (:require [clojure.test :refer :all]
            [stub-http.core :refer :all]
            [cheshire.core :as json]
            [clj-http.lite.client :as client]))

(declare ^:dynamic *stub-server*)

(defn start-and-stop-stub-server [f]
  (binding [*stub-server* (start! {{:method :get :path "/something" :query-params {:name "value"}}
                                   {:status 200 :content-type "application/json" :body (json/generate-string {:hello "world"})}})]
    (try
      (f)
      (finally
        (.close *stub-server*)))))

(use-fixtures :each start-and-stop-stub-server)

(deftest Example2
    (let [response (client/get (str (:uri *stub-server*) "/something"))
          json-response (json/parse-string (:body response) true)]
      (is (= "world" (:hello json-response)))))
```

### Example 3 - Using start! and with-open

The `start!` function return a Clojure record that implements [Closable](https://docs.oracle.com/javase/8/docs/api/java/io/Closeable.html). 
This means that you can use it with the [with-open](https://clojuredocs.org/clojure.core/with-open) macro:
  
```clojure
(ns stub-http.example3
  (:require [clojure.test :refer :all]
            [stub-http.core :refer :all]
            [cheshire.core :as json]
            [clj-http.lite.client :as client]))

(deftest Example3
  (with-open [server (start! {"/something" {:status 200 :content-type "application/json"
                                            :body   (json/generate-string {:hello "world"})}})]
      (let [response (client/get (str (:uri server) "/something"))
            json-response (json/parse-string (:body response) true)]
        (is (= "world" (:hello json-response)))))
```

## Project Status

The project is in an early phase and changes are expected

[![Project Stats](https://www.openhub.net/p/stub-http/widgets/project_thin_badge.gif)](https://www.openhub.net/p/stub-http)

## License

Copyright (c) 2016 Johan Haleby

Released under [the MIT License](http://www.opensource.org/licenses/mit-license.php).

## [Contributors](https://github.com/johanhaleby/stub-http/contributors)
