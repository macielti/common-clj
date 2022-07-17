(ns fixtures.components
  (:require [fixtures.config]
            [telegrambot-lib.core :as telegram-bot]))

(def telegram (telegram-bot/create fixtures.config/token))

(def components-for-telegram
  {:telegram telegram
   :config   fixtures.config/config})
