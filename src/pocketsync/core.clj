(ns pocketsync.core
  (:gen-class)
  (:require [environ.core :refer [env]]
            [clj-http.client :as client]
            [taoensso.carmine :as car :refer [wcar]]
            [clojure.data.json :as json]
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [ring.adapter.hetrry :as jetty])

(def redis-conn {:pool nil :spec {:uri (env :rediscloud-url)}})
(defmacro wcar*
  [& body]
  `(car/wcar redis-conn ~@body))

(def retrieve-url "https://getpocket.com/v3/get")
(def consumer-key (env :consumer-key))
(def ujjwal-token (env :ujjwal-token))
(def ronak-token (env :ronak-token))
(def tag (env :tag))

(defn items
  [token]
  (set (map #(get % "given_url")
         (-> (client/post retrieve-url
                          {:form-params
                           {:consumer_key consumer-key
                            :access_token token
                            :detailType "complete"
                            :tag tag}})
             :body
             json/read-str
             (get "list")
             vals))))

(def add-url "https://getpocket.com/v3/add")

(defn add-item
  [token item]
  (client/post add-url
               {:form-params
                {:consumer_key consumer-key
                 :access_token token
                 :url item
                 :tags [tag]}}))

(defn set-interval [callback ms] 
  (future (while true (do (Thread/sleep ms) (callback)))))

(defn pocketsync
  []
  (let [ujjwal (items ujjwal-token)
        ronak (items ronak-token)
        for-ujjwal (clojure.set/difference ronak ujjwal)
        for-ronak (clojure.set/difference ujjwal ronak)]
    (dorun (map #(add-item ujjwal-token %) for-ujjwal))
    (dorun (map #(add-item ronak-token %) for-ronak))))

(defroutes app
  (GET "*" []
       {:status 200
        :headers {"CONTENT-TYPE" "text/plain"}
        :body "Leaves the friends alone to their pocketsyncing"}))

(defn -main
  [& args]
  (do
    (pocketsync)
    (set-interval pocketsync (* 1000 60 15))
    (let [port (Integer. (or port (env :port) 5000))]
      (jetty/run-jetty (site #'app) {:port port :join? false})))) ; Sync every 15 minutes
