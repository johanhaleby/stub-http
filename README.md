# stub-http [![Build Status](https://img.shields.io/travis/myfreeweb/clj-http-stub.svg?style=flat)](https://travis-ci.org/johanhaleby/stub-http) [![MIT License](https://img.shields.io/badge/license-MIT-brightgreen.svg?style=flat)](https://www.tldrlegal.com/l/mit)

A Clojure library designed to stub HTTP responses regardless of which library used to actually make the HTTP requests.
  
There are several library specific "http mocking/stubbing/faking" libraries out there such as [clj-http-fake](https://github.com/myfreeweb/clj-http-fake) and 
[ring-mock](https://github.com/ring-clojure/ring-mock) but they won't work unless you're using a specific library. I couldn't find a library agnostic library for 
faking HTTP responses so I sat out to write one myself based on [nanohttpd](https://github.com/NanoHttpd/nanohttpd). This is useful
if you want to test your app against a "real" HTTP server with actual HTTP requests. And even if you don't _want_ to this it may be your only
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

## Full Examples

### Example 1
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

## Project Status

The project is in an early phase and changes are expected

## License

Copyright (c) 2016 Johan Haleby

Released under [the MIT License](http://www.opensource.org/licenses/mit-license.php).

## [Contributors](https://github.com/johanhaleby/stub-http/contributors)
