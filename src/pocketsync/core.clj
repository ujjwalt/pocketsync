(ns pocketsync.core
  (:gen-class)
  (:require [environ.core :refer [env]])
  (:require [clj-http.client :as client])
  (:require [taoensso.carmine :as car :refer [wcar]])
  (:require [clojure.data.json :as json]))

(def redis-conn {:pool nil :spec {:uri (env :rediscloud-url)}})
(defmacro wcar*
  [& body]
  `(car/wcar redis-conn ~@body))

(def since (wcar* (car/get "since")))
(def retrieve-url "https://getpocket.com/v3/get")
(def consumer-key (env :consumer-key))
(def ujjwal-token (env :ujjwal-token))
(def ronak-token (env :ronak-token))
(def tag (env :tag))

(defn -main
  [& args]
  (do
    (pocketsync)
    (set-interval pocketsync 3600000))) ; Sync every 1 hour

(defn pocketsync
  []
  (let [ujjwal (items ujjwal-token)
        ronak (items ronak-token)
        for-ujjwal (clojure.set/difference ronak ujjwal)
        for-ronak (clojure.set/difference ujjwal ronak)]
    (add-items ujjwal-token for-ujjwal)
    (add-items ronak-token for-ronak)
    (wcar* (car/set "since" (quot (System/currentTimeMillis) 1000)))))

(defn items
  [token]
  (-> (client/post retrieve-url
                   {:form-params
                    {:consumer_key consumer-key
                     :access_token token
                     :since since
                     :tag tag}})
      :body
      json/read-str
      (get "list")
      vals
      set))

(def add-url "https://getpocket.com/v3/add")

(defn add-items
  [token items]
  (-> #(client/post add-url
                   {:form-params
                    {:consumer_key consumer-key
                     :access_token token
                     :url (get % "given_url")
                     :tags [tag]}})
      (map items)))

(defn set-interval [callback ms] 
  (future (while true (do (Thread/sleep ms) (callback)))))

