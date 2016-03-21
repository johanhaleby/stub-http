(defproject se.haleby/fake-http "0.1.4"
  :description "A client library agonistic way to fake HTTP responses"
  :url "https://github.com/johanhaleby/fake-http"
  :license {:name         "MIT License"
            :url          "http://www.opensource.org/licenses/mit-license.php"
            :distribution :repo}
  :lein-release {:deploy-via :clojars}
  :min-lein-version "2.0.0"
  :dependencies [[org.nanohttpd/nanohttpd "2.3.0"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]
                                  [cheshire "5.5.0"]
                                  [clj-http-lite "0.3.0"]]}}
  :plugins [[lein-codox "0.9.4"]])
