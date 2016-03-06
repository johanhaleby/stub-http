# fake-http

A Clojure library designed to fake HTTP responses. 

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
