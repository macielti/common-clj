(ns common-clj.integrant-components.rate-limiter
  (:require [clj-rate-limiter.core :as r]
            [integrant.core :as ig]
            [schema.core :as s]
            [taoensso.timbre :as log])
  (:import (clj_rate_limiter.core MemoryRateLimiterFactory)))

(s/defschema RateLimitersDefinition
  {s/Keyword MemoryRateLimiterFactory})

(defmethod ig/init-key ::rate-limiter
  [_ {:keys [rate-limiters-definition]}]
  (log/info :starting ::rate-limiter)
  (let [rate-limiter (atom {})]

    (s/validate RateLimitersDefinition rate-limiters-definition)

    (doseq [rate-limiter-key (keys rate-limiters-definition)
            :let [rate-limiter-config (get rate-limiters-definition rate-limiter-key)]]
      (reset! rate-limiter (assoc @rate-limiter rate-limiter-key (r/create rate-limiter-config))))

    rate-limiter))

(defmethod ig/halt-key! ::rate-limiter
  [_ _routes]
  (log/info :stopping ::rate-limiter))
