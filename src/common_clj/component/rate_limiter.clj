(ns common-clj.component.rate-limiter
  (:require [clj-rate-limiter.core :as r]
            [com.stuartsierra.component :as component]
            [schema.core :as s])
  (:import (clj_rate_limiter.core MemoryRateLimiterFactory)))

(s/defschema RateLimitersDefinition
  {s/Keyword MemoryRateLimiterFactory})

(defrecord RateLimiter [rate-limiters-definition]
  component/Lifecycle
  (start [component]
    (let [rate-limiter (atom {})]

      (s/validate RateLimitersDefinition rate-limiters-definition)

      (doseq [rate-limiter-key (keys rate-limiters-definition)
              :let [rate-limiter-config (get rate-limiters-definition rate-limiter-key)]]
        (reset! rate-limiter (assoc @rate-limiter rate-limiter-key (r/create rate-limiter-config))))

      (merge component {:rate-limiter rate-limiter})))

  (stop [component]
    (assoc component :rate-limiter nil)))

(defn new-rate-limiter
  [rate-limiters-definition]
  (map->RateLimiter {:rate-limiters-definition rate-limiters-definition}))
