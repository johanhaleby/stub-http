(defproject se.haleby/stub-http "0.2.1"
  :description "A client library agonistic way to stub HTTP responses"
  :url "https://github.com/johanhaleby/stub-http"
  :license {:name         "MIT License"
            :url          "http://www.opensource.org/licenses/mit-license.php"
            :distribution :repo}
  :lein-release {:deploy-via :clojars}
  :min-lein-version "2.0.0"
  :dependencies [[org.nanohttpd/nanohttpd "2.3.0"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]
                                  [cheshire "5.5.0"]
                                  [clj-http-lite "0.3.0"]]}}
  :repositories [["releases" {:url "http://clojars.org/repo" :creds :gpg}]]
  :plugins [[lein-codox "0.9.4"]])
