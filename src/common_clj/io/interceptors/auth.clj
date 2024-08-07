(ns common-clj.io.interceptors.auth
  (:require [cheshire.core :as json]
            [clj-http.client :as client]
            [common-clj.error.core :as common-error]
            [common-clj.schema.core :as common-schema]
            [io.pedestal.interceptor :as pedestal.interceptor]
            [medley.core :as medley]
            [schema.core :as s]
            [taoensso.timbre :as log]))

(s/defschema GoogleRecaptchaV3ResponseTokenValidationResultWireIn
  (common-schema/loose-schema {:success                s/Bool
                               (s/optional-key :score) Double}))

(s/defschema GoogleRecaptchaV3ResponseTokenValidationResult
  {:validation-result/success                s/Bool
   (s/optional-key :validation-result/score) Double})

(s/defn wire->google-recaptcha-v3-response-token-validation-result :- GoogleRecaptchaV3ResponseTokenValidationResult
  [{:keys [success score]} :- GoogleRecaptchaV3ResponseTokenValidationResultWireIn]
  (medley/assoc-some {:validation-result/success success}
                     :validation-result/score score))

(s/defn ^:private validate-recaptcha-v3-token! :- GoogleRecaptchaV3ResponseTokenValidationResult
  [response-token :- s/Str
   secret-token :- s/Str]
  (-> (client/post "https://www.google.com/recaptcha/api/siteverify" {:accept       :json
                                                                      :query-params {"secret"   secret-token
                                                                                     "response" response-token}})
      :body
      (json/decode true)
      wire->google-recaptcha-v3-response-token-validation-result))

;TODO: Maybe in the future, we should be able to receive the expected minimal threshold value for score
(s/defn valid-recaptcha-v3-response-check? :- s/Bool
  [{:validation-result/keys [success score] :as google-recaptcha-response} :- GoogleRecaptchaV3ResponseTokenValidationResult]
  (log/info google-recaptcha-response)
  (and success
       (>= score 0.5)))

(def recaptcha-v3-validation-interceptor
  (pedestal.interceptor/interceptor
   {:name  ::recaptcha-validation-interceptor
    :enter (fn [{{:keys [components headers]} :request :as context}]
             (let [recaptcha-response-token (get headers "x-recaptcha-token" "missing")
                   recaptcha-secret-token (some-> components :config :recaptcha-secret-token)
                   recaptcha-result-check (when recaptcha-secret-token
                                            (-> (validate-recaptcha-v3-token! recaptcha-response-token recaptcha-secret-token)
                                                valid-recaptcha-v3-response-check?))]
               (when (and recaptcha-secret-token (not recaptcha-result-check))
                 (common-error/http-friendly-exception 400
                                                       "not-able-to-perform-recaptcha-validation"
                                                       "Not able to check the success completion of the reCAPTCHA challenge"
                                                       {:error :not-able-to-perform-recaptcha-validation})))
             context)}))
