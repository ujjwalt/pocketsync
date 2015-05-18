(defproject pocketsync "0.1.0-SNAPSHOT"
  :description "An app that psyncs the pockets of friends"
  :url "http://arcane-scrubland-5832.herokuapp.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [environ "1.0.0"]
                 [clj-http "1.1.2"]
                 [com.taoensso/carmine "2.10.0"]
                 [org.clojure/data.json "0.2.6"]
                 [compojure "1.3.4"]
                 [ring/ring-jetty-adapter "1.4.0-RC1"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :min-lein-version "2.0.0"
  :plugins [[lein-environ "1.0.0"]]
  :uberjar-name "pocketsync.jar"
  :env {:consumer-key "40980-7dfb1cc462de5c75c3fe6f91"
        :rediscloud-url "redis://127.0.0.1:6379"
        :ronak-token "fd13e720-2ed7-56d0-c627-1a9143"
        :tag "beacon"
        :ujjwal-token "b3368125-f443-3281-e6a4-90c318"})
