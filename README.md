# fake-http

A Clojure library designed to fake HTTP responses regardless of which library used to actually make the HTTP requests.
  
There are several library specific "http faking" libraries out there such as [clj-http-fake](https://github.com/myfreeweb/clj-http-fake) and 
[ring-mock](https://github.com/ring-clojure/ring-mock) but they require that you a specific library. I couldn't find a library agnostic library for 
faking HTTP responses so I sat out the write my own based on MockWebServer in [okhttp](http://square.github.io/okhttp/). This is useful
if you want to test your app against a "real" HTTP server with actual HTTP requests. And even if you don't _want_ to do it it may be your only
option if you're (for example) your clojure app is using a Java library to make HTTP requests.

## Usage

```clojure
(let [fake-server (fake-server/start!)
        (fake-route! fake-server "/x" {:status 200 :content-type "application/json" :body (slurp (io/resource "my.json"))})
        (fake-route! fake-server {:path "/y" :q "something")} {:status 200 :content-type "application/json" :body (slurp (io/resource "my2.json"))})]
        ; Do actual HTTP request
         (shutdown! fake-server))
```


## License

Copyright © 2016 Johan Haleby

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
