(ns practicalli.game-scoreboard
  (:gen-class)
  (:require
   #_[reitit.core :as reitit]
   [reitit.ring :as ring]
   [reitit.ring.middleware.parameters :as parameters]
   [muuntaja.core :as muuntaja]
   [reitit.ring.middleware.muuntaja :as middleware-muuntaja]
   [reitit.coercion.spec]
   [reitit.ring.coercion :as coercion]

   ;; Self-documenting API
   [reitit.swagger :as api-docs]
   [reitit.swagger-ui :as api-docs-ui]

   ;; System
   [ring.adapter.jetty :as app-server]
   [com.brunobonacci.mulog :as mulog]
   ))

;; TODO: Define start/stop functions for jetty
;; TODO: Add status route
;; TODO: add swagger UI and swagger data to routes
;; TODO: Add integrant REPL

(def status (constantly {:status 200 :body "I am okay, thank you"}))

(def routes-system
  ["/system"
   {:swagger {:tags        ["Application Support"]
              :description "ScoreBoard API system tools"}}
   ["/status" {:get {:summary "Report on Status of StatsBomb Entitlement Service"
                     :handler status}}]

   ["/echo-request" {:get {:summary "Echo the original request to aid development"
                           :handler (fn [request] {:status 200 :body (str request)})}}]])

(def open-api-docs
  ["/swagger.json"
   {:get {:no-doc  true
          :swagger {:info {:title "Global e-games Scoreboard APIs"}}
          :handler (api-docs/create-swagger-handler)}}])

(def routes-scoreboard
  ["/api"
   {:swagger {:tags ["Games Scoreboard"]
              :description "Scoreboard API Service Games Scoreboards"}}
   ["/scoreboard"
    ["/scores"  {:get {:handler status}}]]])

(def app
  (ring/ring-handler
    (ring/router
      [open-api-docs
       routes-system
       routes-scoreboard]

      ;; router data affecting all routes
      {:data {:coercion   reitit.coercion.spec/coercion
              :muuntaja   muuntaja/instance
              :middleware [;; swagger feature
                           api-docs/swagger-feature
                           ;; query-params & form-params
                           parameters/parameters-middleware
                           ;; content-negotiation
                           middleware-muuntaja/format-middleware
                           coercion/coerce-response-middleware
                           coercion/coerce-request-middleware
                           ]}})

    (ring/routes
      ;; Open API documentation as default route
      (api-docs-ui/create-swagger-ui-handler {:path "/"})

      ;; Respond to any other route - returns blank page
      ;; TODO: create custom handler for routes not recognised
      (ring/create-default-handler))
    ))


(defn greet
  "Callable entry point to the application."
  [data]
  (println (str "Hello, " (or (:name data) "World") "!")))


;; System

(defn app-server-start
  [port]
  (mulog/log ::app-server-start :message (str "Starting Jetty on port " port))
  (app-server/run-jetty #'app {:port port :join? false}))


(defn -main [& [port]]
  (let [port (Integer. (or port
                           (System/getenv "PORT")
                           8888))]

    ;; Start publishing mulog event logs
    (mulog/start-publisher! {:type :console})
    (mulog/log ::main :message (str "-main called with port value " port))
    ;; Start application server
    (app-server-start port)
    ))


;; REPL driven development

(comment

  ;; Start API Service
  (def app-server-instance (-main 8888))

  ;; Stop API Service
  (.stop app-server-instance)


  ;; Test out routes in the REPL
  (app {:request-method :get
        :uri            "/system/status"})
;; => {:status 200, :body "I am okay, thank you"}

#_())
