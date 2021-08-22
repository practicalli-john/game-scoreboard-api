(ns practicalli.design-journal)


;; Rich comment block with redefined vars ignored


#_{:clj-kondo/ignore [:redefined-var]}
(comment

  ;; Require reitit to use its functions
  (require '[reitit.core :as reitit])

  ;; Define simple routes with names
  (def router
    (reitit/router
     [["/hello" ::hello]
      ["/api/ping" ::ping]
      ["/api/game-scoreboard/:score-id" ::game-score]]))

  ;; Get details of route by path
  (reitit/match-by-path router "/api/ping")
  ;; => {:template "/api/ping",
  ;;     :data {:name :practicalli.design-journal/ping},
  ;;     :result nil,
  ;;     :path-params {},
  ;;     :path "/api/ping"}

  ;; The name is shown as the fully qualified keyword name
  ;; results and path-parameters have not yet been set

  (reitit/match-by-path router "/api/game-scoreboard/:score-id")
  ;; => {:template "/api/game-scoreboard/:score-id",
  ;;     :data
  ;;     {:name :practicalli.design-journal/game-score,
  ;;      :get #function[clojure.core/constantly/fn--5689]},
  ;;     :result nil,
  ;;     :path-params {:score-id ":score-id"},
  ;;     :path "/api/game-scoreboard/:score-id"}

;; Create a UUID value to use as a random score-id
  (java.util.UUID/randomUUID)
  ;; => #uuid "296e9192-683c-405e-9a87-aa11e27a168e"

  (reitit/match-by-name router ::game-score {:score-id "296e9192-683c-405e-9a87-aa11e27a168e"})
  ;; => {:template "/api/game-scoreboard/:score-id",
  ;;     :data
  ;;     {:name :practicalli.design-journal/game-score,
  ;;      :get #function[clojure.core/constantly/fn--5689]},
  ;;     :result nil,
  ;;     :path-params {:score-id "296e9192-683c-405e-9a87-aa11e27a168e"},
  ;;     :path "/api/game-scoreboard/296e9192-683c-405e-9a87-aa11e27a168e"}

;; Adding http request types
  ;; Refactor routes to define how to respond to a get request

  (def router
    (reitit/router
     [["/hello"
       {:name ::root
        :get (constantly {:status 200 :body "Hello Reitit"})}]

      ["/api/ping"
       {:name ::ping
        :get (constantly {:status 200 :body "pong"})}]

      ["/api/game-scoreboard/:score-id"
       {:name ::game-score
        :get (constantly {:status 200 :body {:score 42}})}]]))

  ;; NOTE: the name keyword of the route must be moved to the hash-map defining the route
  ;; using the :name key, otherwise nil will be return rather than the route details

  (reitit/match-by-path router "/api/ping")
;; => {:template "/api/ping",
;;     :data
;;     {:name :practicalli.design-journal/ping,
;;      :get #function[clojure.core/constantly/fn--5689]},
;;     :result nil,
;;     :path-params {},
;;     :path "/api/ping"}

  (reitit/match-by-name router ::game-score)
;; => {:template "/api/game-scoreboard/:score-id",
;;     :data
;;     {:name :practicalli.design-journal/game-score,
;;      :get #function[clojure.core/constantly/fn--5689]},
;;     :result nil,
;;     :path-params nil,
;;     :required #{:score-id}}

  (reitit/routes router)
  ;; => [["/"
  ;;      {:name :practicalli.design-journal/root,
  ;;       :get #function[clojure.core/constantly/fn--5689]}]
  ;;     ["/api/ping"
  ;;      {:name :practicalli.design-journal/ping,
  ;;       :get #function[clojure.core/constantly/fn--5689]}]
  ;;     ["/api/game-scoreboard/:score-id"
  ;;      {:name :practicalli.design-journal/game-score,
  ;;       :get #function[clojure.core/constantly/fn--5689]}]]

  ;; Now we have some routes defined, place them into a handler so we can call them

  (require '[reitit.ring :as ring])

  (def app
    (ring/ring-handler
     (ring/router
      [["/hello"
        {:name ::root
         :get (constantly {:status 200 :body "Hello Reitit"})}]

       ["/api/ping"
        {:name ::ping
         :get (constantly {:status 200 :body "pong"})}]

       ["/echo-request"
        {:get {:name ::echo-request
               :summary "Echo the original request"
               :handler (fn [request] {:status 200 :body (str request)})}}]


       ;; What is the shape of the request for a post with path parameters
       ["/api/game-scoreboard/:score-id"
        {:name ::game-score
         :get (fn [request] {:status 200 :body (str request)})}]

       #_(fn [{{{:keys score-id} :query} :parameters}]
           {:status 200
            :body {:score score-id}})

       ])))

  (app {:request-method :get
        :uri "/hello"})
;; => {:status 200, :body "Hello Reitit"}

  (app {:request-method :get
        :uri "/api/ping"})
;; => {:status 200, :body "pong"}

  (app {:request-method :get
        :uri "/echo-request"})
;; => {:status 200,
;;     :body
;;     "{:request-method :get, :uri \"/echo-request\", :path-params {}, :reitit.core/match #reitit.core.Match{:template \"/echo-request\", :data {:get {:name :practicalli.design-journal/echo-request, :summary \"Echo the original request\", :handler #function[practicalli.design-journal/fn--11836]}}, :result #reitit.ring.Methods{:get #reitit.ring.Endpoint{:data {:name :practicalli.design-journal/echo-request, :summary \"Echo the original request\", :handler #function[practicalli.design-journal/fn--11836]}, :handler #function[practicalli.design-journal/fn--11836], :path \"/echo-request\", :method :get, :middleware []}, :head nil, :post nil, :put nil, :delete nil, :connect nil, :options #reitit.ring.Endpoint{:data {:no-doc true, :handler #function[reitit.ring/fn--7340/fn--7349]}, :handler #function[reitit.ring/fn--7340/fn--7349], :path \"/echo-request\", :method :options, :middleware []}, :trace nil, :patch nil}, :path-params {}, :path \"/echo-request\"}, :reitit.core/router #object[reitit.core$lookup_router$reify__6763 0x6ce5e35a \"reitit.core$lookup_router$reify__6763@6ce5e35a\"]}"}

  ;; Body structure of the response

  #_{:request-method :get,
   :uri \"/echo-request\",
   :path-params {},
   :reitit.core/match
   #reitit.core.Match
   {:template \"/echo-request\",

    :data {:get
           {:name :practicalli.design-journal/echo-request,
            :summary \"Echo the original request\",
            :handler #function[practicalli.design-journal/fn--11836]}},
    :result #reitit.ring.Methods
    {:get #reitit.ring.Endpoint
     {:data
      {:name :practicalli.design-journal/echo-request,
       :summary \"Echo the original request\",
       :handler #function[practicalli.design-journal/fn--11836]},
      :handler #function[practicalli.design-journal/fn--11836],
      :path \"/echo-request\",
      :method :get,
      :middleware []},
     :head nil,
     :post nil,
     :put nil,
     :delete nil,
     :connect nil,
     :options #reitit.ring.Endpoint
     {:data
      {:no-doc true,
       :handler #function[reitit.ring/fn--7340/fn--7349]},
      :handler #function[reitit.ring/fn--7340/fn--7349],
      :path \"/echo-request\",
      :method :options,
      :middleware []},
     :trace nil,
     :patch nil},
    :path-params {},
    :path \"/echo-request\"},
   :reitit.core/router #object[reitit.core$lookup_router$reify__6763 0x6ce5e35a \"reitit.core$lookup_router$reify__6763@6ce5e35a\"]}


  (app {:request-method :get
          :uri "/api/game-scoreboard/:score-id"
          :query-params {:score-id "296e9192-683c-405e-9a87-aa11e27a168e"}})
;; => {:status 200,
;;     :body
;;     "{:request-method :get, :uri \"/api/game-scoreboard/:score-id\", :query-params {:score-id \"296e9192-683c-405e-9a87-aa11e27a168e\"}, :path-params {:score-id \":score-id\"}, :reitit.core/match #reitit.core.Match{:template \"/api/game-scoreboard/:score-id\", :data {:name :practicalli.design-journal/game-score, :get {:handler #function[practicalli.design-journal/fn--11848]}}, :result #reitit.ring.Methods{:get #reitit.ring.Endpoint{:data {:name :practicalli.design-journal/game-score, :handler #function[practicalli.design-journal/fn--11848]}, :handler #function[practicalli.design-journal/fn--11848], :path \"/api/game-scoreboard/:score-id\", :method :get, :middleware []}, :head nil, :post nil, :put nil, :delete nil, :connect nil, :options #reitit.ring.Endpoint{:data {:name :practicalli.design-journal/game-score, :no-doc true, :handler #function[reitit.ring/fn--7340/fn--7349]}, :handler #function[reitit.ring/fn--7340/fn--7349], :path \"/api/game-scoreboard/:score-id\", :method :options, :middleware []}, :trace nil, :patch nil}, :path-params {:score-id \":score-id\"}, :path \"/api/game-scoreboard/:score-id\"}, :reitit.core/router #object[reitit.core$mixed_router$reify__6808 0x44fac419 \"reitit.core$mixed_router$reify__6808@44fac419\"]}"}

  ;; Body structure of the response
  #_{:request-method :get,
   :uri \"/api/game-scoreboard/:score-id\",
   :query-params {:score-id \"296e9192-683c-405e-9a87-aa11e27a168e\"},
   :path-params {:score-id \":score-id\"},

     :reitit.core/match #reitit.core.Match
     {:template \"/api/game-scoreboard/:score-id\",
      :data {:name :practicalli.design-journal/game-score,
             :get {:handler #function[practicalli.design-journal/fn--11848]}},
      :result #reitit.ring.Methods
      {:get #reitit.ring.Endpoint
       {:data
        {:name :practicalli.design-journal/game-score,
         :handler #function[practicalli.design-journal/fn--11848]},
        :handler #function[practicalli.design-journal/fn--11848],
        :path \"/api/game-scoreboard/:score-id\",
        :method :get,
        :middleware []},
       :head nil,
       :post nil,
       :put nil,
       :delete nil,
       :connect nil,
       :options #reitit.ring.Endpoint
       {:data
        {:name :practicalli.design-journal/game-score,
         :no-doc true,
         :handler #function[reitit.ring/fn--7340/fn--7349]},
        :handler #function[reitit.ring/fn--7340/fn--7349],
        :path \"/api/game-scoreboard/:score-id\",
        :method :options,
        :middleware []},
       :trace nil,
       :patch nil},
      :path-params {:score-id \":score-id\"},
      :path \"/api/game-scoreboard/:score-id\"},
   :reitit.core/router #object[reitit.core$mixed_router$reify__6808 0x44fac419 \"reitit.core$mixed_router$reify__6808@44fac419\"]}


  #_()) ;; End of rich comment block




;; Rich comment block with redefined vars ignored
#_{:clj-kondo/ignore [:redefined-var]}
(comment

(require '[muuntaja.core :as m])
(require '[reitit.coercion.spec])
(require '[reitit.ring.coercion :as coercion])
(require '[reitit.ring.middleware.muuntaja :as muuntaja])
(require '[reitit.ring.middleware.parameters :as parameters])
(require '[reitit.ring.middleware.dev :as reitit-dev])

  (def app2
    (ring/ring-handler
     (ring/router
      [["/hello"
        {:name ::root
         :get (constantly {:status 200 :body "Hello Reitit"})}]

       ["/api/ping"
        {:name ::ping
         :get (constantly {:status 200 :body "pong"})}]

       ["/echo-request"
        {:get {:name ::echo-request
               :summary "Echo the original request"
               :handler (fn [request] {:status 200 :body (str request)})}}]


       ;; What is the shape of the request for a post with path parameters
       ["/api/game-scoreboard/:score-id"
        {:name ::game-score
         :get (fn [request]
                (let [query-paramters (:query-params request)
                      score-id (:score-id query-paramters)]
           {:status 200
            :body {:score score-id}}))}]

       ;; The same but using destructuring
       ["/api/game-scores/:score-id"
        {:name ::game-scores
         :get {:parameters {:query {:score-id uuid?}}
               :responses {200 {:body {:score-id uuid?}}}
               :handler (fn [{{{:keys [score-id]} :query} :parameters}]
                {:status 200
                 :body {:score-id score-id}}
                )} }]]
      {}
      #_{:data {:reitit.middleware/transform reitit-dev/print-request-diffs ;; pretty diffs
              :coercion   reitit.coercion.spec/coercion
              :muuntaja   m/instance
              :middleware [parameters/parameters-middleware
                           muuntaja/format-negotiate-middleware
                           ;; encoding response body - returns value as ByteArrayStream
                           #_muuntaja/format-response-middleware
                           coercion/coerce-response-middleware
                           coercion/coerce-request-middleware
                           ]}}
      )))

(app2 {:request-method :get
          :uri "/api/game-scoreboard/:score-id"
          :query-params {:score-id "296e9192-683c-405e-9a87-aa11e27a168e"}})
;; => {:status 200, :body {:score "296e9192-683c-405e-9a87-aa11e27a168e"}}


;; Coercion and response formatting middleware

(def app-coercion-formatting
    (ring/ring-handler
     (ring/router
      [["/hello"
        {:name ::root
         :get (constantly {:status 200 :body "Hello Reitit"})}]

       ["/api/ping"
        {:name ::ping
         :get (constantly {:status 200 :body "pong"})}]

       ["/echo-request"
        {:get {:name ::echo-request
               :summary "Echo the original request"
               :handler (fn [request] {:status 200 :body (str request)})}}]


       ;; What is the shape of the request for a post with path parameters
       ["/api/game-scoreboard/:score-id"
        {:name ::game-score
         :get (fn [request]
                (let [query-paramters (:query-params request)
                      score-id (:score-id query-paramters)]
           {:status 200
            :body {:score score-id}}))}]

       ;; The same but using destructuring
       ["/api/game-scores/:score-id"
        {:name ::game-scores
         :get {:parameters {:query {:score-id uuid?}}
               :responses {200 {:body {:score-id uuid?}}}
               :handler (fn [{{{:keys [score-id]} :query} :parameters}]
                {:status 200
                 :body {:score-id score-id}}
                )} }]]
      {:data {:reitit.middleware/transform reitit-dev/print-request-diffs ;; pretty diffs
              :coercion   reitit.coercion.spec/coercion
              :muuntaja   m/instance
              :middleware [parameters/parameters-middleware
                           muuntaja/format-negotiate-middleware
                           ;; encoding response body - returns value as ByteArrayStream
                           muuntaja/format-response-middleware
                           coercion/coerce-response-middleware
                           coercion/coerce-request-middleware
                           ]}})))

;; With the `muuntaja/format-response-middleware` enabled, the value for :score-id
;; in the body of the response is encoded in a ByteArrayInputStream

;; The score-id value can be read by slurp
(->>
  (app-coercion-formatting {:request-method :get
                            :uri "/api/game-scores/:score-id"
                            :query-params {:score-id #uuid "296e9192-683c-405e-9a87-aa11e27a168e"}})
  :body
  slurp)
;; => {:status 200, :body {:score-id #uuid "296e9192-683c-405e-9a87-aa11e27a168e"}}

;; Or muuntaja can be used to decode the score-id value
(->>
  (app-coercion-formatting {:request-method :get
          :uri "/api/game-scores/:score-id"
         :query-params {:score-id #uuid "296e9192-683c-405e-9a87-aa11e27a168e"}})
  :body
  (m/decode (m/create) "application/json"))
;; => {:score-id "296e9192-683c-405e-9a87-aa11e27a168e"}

  #_()) ;; End of rich comment block
