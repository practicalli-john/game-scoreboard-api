(ns practicalli.design-journal)


;; Rich comment block with redefined vars ignored


#_{:clj-kondo/ignore [:redefined-var]}
(comment

  ;; Require reitit to use its functions
  (require '[reitit.core :as reitit])

  ;; Define simple routes with names
  (def router
    (reitit/router
     [["/api/ping" ::ping]
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

;; Create a UUID value to use as a random score-id
  (java.util.UUID/randomUUID)
  ;; => #uuid "296e9192-683c-405e-9a87-aa11e27a168e"

  (reitit/match-by-name router ::game-score {:score-id "296e9192-683c-405e-9a87-aa11e27a168e"})

  ;; Refactor routes to define how to respond to a get request

  (def routes
    (reitit/router
     [["/"
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
  (reitit/match-by-path routes-core "/")
;; => nil
  (reitit/match-by-name routes-core ::root)
;; => nil
  (reitit/routes routes-core)
;; => []

  (def routes-core-unnamed
    (reitit/router
     [["/"
       {:get (constantly {:status 200 :body "Hello Reitit"})}]]))

  (reitit/match-by-path routes-core-unnamed "/")
;; => #reitit.core.Match{:template "/", :data {:get #function[clojure.core/constantly/fn--5689]}, :result nil, :path-params {}, :path "/"}

  (reitit/routes routes-core-unnamed)
;; => [["/" {:get #function[clojure.core/constantly/fn--5689]}]]

  (def routes-core-named
    (reitit/router
     [["/"
       {:name ::root
        :get (constantly {:status 200 :body "Hello Reitit"})}]]))

  (reitit/match-by-path routes-core-named "/")
;; => #reitit.core.Match{:template "/", :data {:name :practicalli.design-journal/root, :get #function[clojure.core/constantly/fn--5689]}, :result nil, :path-params {}, :path "/"}

  (reitit/match-by-name routes-core-named ::root)
;; => #reitit.core.Match{:template "/", :data {:name :practicalli.design-journal/root, :get #function[clojure.core/constantly/fn--5689]}, :result nil, :path-params {}, :path "/"}

  (reitit/routes routes-core-named)
;; => [["/" {:name :practicalli.design-journal/root, :get #function[clojure.core/constantly/fn--5689]}]]

  #_()) ;; End of rich comment block
