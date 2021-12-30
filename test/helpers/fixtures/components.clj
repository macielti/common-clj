(ns fixtures.components
  (:require [clojure.test :refer :all]
            [telegrambot-lib.core :as telegram-bot]
            [fixtures.config]))

(def telegram (telegram-bot/create fixtures.config/token))

(def components-for-telegram
  {:telegram telegram
   :config   fixtures.config/config})
