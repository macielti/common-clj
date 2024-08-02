(ns common-clj.component.telegram.diplomat.http-client
  (:require [cheshire.core :as json]
            [common-clj.component.http-client :as component.http-client]
            [schema.core :as s]))

(s/defn fetch-telegram-file-path :- s/Str
  [external-file-id :- s/Str
   telegram-token :- s/Str
   http-client]
  (-> {:url     (format "https://api.telegram.org/bot%s/getFile?file_id=%s" telegram-token external-file-id)
       :method  :get
       :payload {:accept :json}}
      (component.http-client/request! http-client)
      :body
      (json/decode true)
      :result
      :file_path))

(s/defn download-telegram-document!
  [file-path :- s/Str
   telegram-token :- s/Str
   http-client]
  (-> {:url     (format "https://api.telegram.org/file/bot%s/%s" telegram-token file-path)
       :method  :get
       :payload {:as :stream}}
      (component.http-client/request! http-client)
      :body))
