(defproject se.haleby/fake-http "0.1.0-SNAPSHOT"
  :description "A client library agonistic way to fake HTTP responses"
  :url "https://github.com/johanhaleby/fake-http"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :lein-release {:deploy-via :clojars}
  :min-lein-version "2.0.0"
  :dependencies [[org.nanohttpd/nanohttpd "2.3.0"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]
                                  [cheshire "5.5.0"]
                                  [clj-http-lite "0.3.0"]]}}
  :plugins [[lein-codox "0.9.4"]])
