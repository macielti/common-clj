(ns fixtures.components
  (:require [clojure.test :refer :all]
            [fixtures.config]
            [telegrambot-lib.core :as telegram-bot]))

(def telegram (telegram-bot/create fixtures.config/token))

(def components-for-telegram
  {:telegram telegram
   :config   fixtures.config/config})
