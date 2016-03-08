(defproject fake-http "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[com.squareup.okhttp3/mockwebserver "3.2.0"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]
                                  [cheshire "5.5.0"]
                                  [clj-http-lite "0.3.0"]]}}
  :plugins [[lein-codox "0.9.4"]])
