(ns common-clj.io.interceptors.auth
  (:require [schema.core :as s]
            [cheshire.core :as json]
            [io.pedestal.interceptor :as pedestal.interceptor]
            [common-clj.error.core :as common-error]
            [clj-http.client :as client]))

(s/defn ^:private validate-recaptcha-token!
  [response-token :- s/Str
   secret-token :- s/Str]
  (-> (client/post "https://www.google.com/recaptcha/api/siteverify" {:accept       :json
                                                                      :query-params {"secret"   secret-token
                                                                                     "response" response-token}})
      :body
      (json/decode true)))

(s/defn valid-recaptcha-response-check? :- s/Bool
  [{:keys [success score]}]
  (and success
       (> score 0.7)))


(def recaptcha-validation-interceptor
  (pedestal.interceptor/interceptor
    {:name  ::recaptcha-validation-interceptor
     :enter (fn [{{:keys [components headers]} :request :as context}]
              (let [recaptcha-response-token (get headers "x-recaptcha-token" "missing")
                    recaptcha-secret-token (some-> components :config :recaptcha-secret-token)
                    recaptcha-result-check (when recaptcha-secret-token
                                             (-> (validate-recaptcha-token! recaptcha-response-token recaptcha-secret-token)
                                                 valid-recaptcha-response-check?))]
                (when (and recaptcha-secret-token (not recaptcha-result-check))
                  (common-error/http-friendly-exception 400
                                                        "not-able-to-perform-recaptcha-validation"
                                                        "Not able to check the success completion of the reCAPTCHA challenge"
                                                        {:error :not-able-to-perform-recaptcha-validation})))
              context)}))